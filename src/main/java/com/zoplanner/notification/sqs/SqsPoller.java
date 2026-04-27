package com.zoplanner.notification.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.BaseEvent;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.deleteevent.DeleteEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.event.reminderevent.ReminderEvent;
import com.zoplanner.notification.notification.NotificationPublisher;
import com.zoplanner.notification.event.directmessage.DirectMessageEvent;
import com.zoplanner.notification.event.schedulecalendar.ScheduleCalendarEvent;
import com.zoplanner.notification.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SqsPoller {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;
    private final boolean pollingEnabled;
    private final NotificationPublisher notificationPublisher;
    private final int maxMessages;
    private final int waitTimeSeconds;
    private final ReminderService reminderService;


    public SqsPoller(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            NotificationPublisher notificationPublisher,
            @Value("${aws.sqs.queue.url:}") String queueUrl,
            @Value("${aws.sqs.polling.enabled:true}") boolean pollingEnabled,
            @Value("${aws.sqs.max.messages:10}") int maxMessages,
            @Value("${aws.sqs.wait.time.seconds:20}") int waitTimeSeconds,
            ReminderService reminderService
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
        this.pollingEnabled = pollingEnabled;
        this.notificationPublisher = notificationPublisher;
        this.maxMessages = maxMessages;
        this.waitTimeSeconds = waitTimeSeconds;
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.polling.delay.ms:5000}")
    public void poll() {
        if (!pollingEnabled) {
            log.debug("SQS polling disabled (aws.sqs.polling.enabled=false).");
            return;
        }
        if (queueUrl == null || queueUrl.isBlank()) {
            log.warn("SQS queue URL is empty (aws.sqs.queue.url=).");
            return;
        }

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .waitTimeSeconds(waitTimeSeconds)
                .messageSystemAttributeNames(MessageSystemAttributeName.SENT_TIMESTAMP)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        if (messages.isEmpty()) {
            log.debug("No messages received from SQS.");
            return;
        }

        for (Message message : messages) {
            handleMessage(message);
        }
    }

    private void handleMessage(Message message) {
        String rawBody = message.body();

        try {
            // 0) Poison-skydd: om det inte ens är JSON -> delete direkt (undvik loop)
            if (rawBody == null || !rawBody.trim().startsWith("{")) {
                log.warn("Non-JSON message received. Deleting. messageId={} SentTs={}",
                        message.messageId(), sentTimestamp(message));
                deleteMessage(message);
                return;
            }

            // 1) Unwrap om det är SNS-envelope (Type/Message om Message innehåller JSON-string)
            String payloadJson = unwrapIfSnsEnvelope(rawBody);
            log.info("payloadJson(400)={}", payloadJson.substring(0, Math.min(400, payloadJson.length())));

            // 2) Läs enbart eventType
            BaseEvent base = objectMapper.readValue(payloadJson, BaseEvent.class);
            String eventType = base.eventType();

            log.info("Received SQS messageId={} SentTs={} eventType={}",
                    message.messageId(), sentTimestamp(message), eventType);

            // 3) Saknar eventType -> delete (annars loopar det)
            if (eventType == null || eventType.isBlank()) {
                log.warn("Missing eventType. Deleting messageId={}", message.messageId());
                deleteMessage(message);
                return;
            }

            // 4) Route på eventType

            switch (eventType.trim().toUpperCase()) {
                case "NEW_ASSIGNMENT" -> {
                    NewAssignmentEvent event = objectMapper.readValue(payloadJson, NewAssignmentEvent.class);

                    log.info("Handling NEW_ASSIGNMENT messageId={} eventId={}",
                            message.messageId(), safeEventId(event.eventId()));

                    notificationPublisher.publishNewAssignment(event);
                    deleteMessage(message);
                }

                case "SCHEDULE_UPDATED", "SCHEDULE_UPDATE" -> {
                    // OBS: tillåt både SCHEDULE_UPDATED och SCHEDULE_UPDATE för kompatibilitet
                    ScheduleUpdateEvent event = objectMapper.readValue(payloadJson, ScheduleUpdateEvent.class);

                    log.info("Handling SCHEDULE_UPDATED messageId={} teacherId={} preference={}",
                            message.messageId(),
                            safe(event.getTeacherId()),
                            event.getPreference() != null ? event.getPreference().name() : "-");

                    notificationPublisher.publishScheduleUpdate(event);
                    deleteMessage(message);
                }

                case "DIRECT_MESSAGE", "DIRECT_MSG" -> {
                    DirectMessageEvent event = objectMapper.readValue(payloadJson, DirectMessageEvent.class);

                    log.info("Handling DIRECT_MESSAGE messageId={} eventId={} to={} subject={}",
                            message.messageId(),
                            safeEventId(event.eventId()),
                            safe(event.recipientEmail()),
                            safe(event.subject()));

                    notificationPublisher.publishDirectMessage(event);
                    deleteMessage(message);
                }

                case "SCHEDULE_CALENDAR", "SCHEDULE_EXPORT" -> {
                    ScheduleCalendarEvent event = objectMapper.readValue(payloadJson, ScheduleCalendarEvent.class);

                    log.info("Handling SCHEDULE_CALENDAR messageId={} to={} teacher={}",
                            message.messageId(),
                            safe(event.recipientEmail()),
                            safe(event.teacherName()));

                    notificationPublisher.publishScheduleCalendar(event);
                    deleteMessage(message);
                }

                case "ASSIGNMENT_DELETED", "ASSIGNMENT_DELETE" -> {

                    DeleteEvent event =
                            objectMapper.readValue(payloadJson, DeleteEvent.class);

                    log.info("Handling ASSIGNMENT_DELETED messageId={} eventId={}",
                            message.messageId(),
                            safe(String.valueOf(event.eventId())));

                    notificationPublisher.publishAssignmentDeleted(event);

                    deleteMessage(message);
                }

                case "REMINDER_CREATED", "REMINDER" -> {

                    ReminderEvent event =
                            objectMapper.readValue(payloadJson, ReminderEvent.class);

                    log.info("Received REMINDER from SQS messageId={} email={} sendAt={}",
                            message.messageId(),
                            event.teacherEmail(),
                            event.sendAt());

                    reminderService.saveReminder(event);

                    log.info("Saved REMINDER to DB for email={}", event.teacherEmail());

                    deleteMessage(message);
                }

                default -> {
                    // Okänd / ej stödd -> delete för att undvika retry-loop
                    log.warn("Unsupported eventType='{}'. Deleting messageId={}", eventType, message.messageId());
                    deleteMessage(message);
                }
            }

        } catch (Exception e) {
            log.error("Failed processing SQS messageId={}. Body={}. May be retried or moved to DLQ",
                    message.messageId(), rawBody, e);
            // Deletar INTE → SQS räknar som misslyckat försök → hamnar i DLQ efter 3 försök
        }
    }

    /**
     * If body is an SNS envelope, return the inner Message JSON.
     * Otherwise return body as-is.
     */
    private String unwrapIfSnsEnvelope(String body) {
        try {
            // SNS envelope har ofta "Type" och "Message"
            if (body != null && body.contains("\"Type\"") && body.contains("\"Message\"")) {

                SnsEnvelope env = objectMapper.readValue(body, SnsEnvelope.class);

                if (env != null && env.message() != null) {
                    String msg = env.message().trim();

                    // Case 1: Message är redan en JSON-object string: { ... }
                    if (msg.startsWith("{")) {
                        return msg;
                    }

                    // Case 2: Message är en JSON-string som innehåller JSON (escaped)
                    // Ex: "{\"eventType\":\"SCHEDULE_UPDATED\",\"teacherId\":\"teacher-3\"}"
                    if (msg.startsWith("\"") && msg.endsWith("\"")) {
                        String unescaped = objectMapper.readValue(msg, String.class);
                        if (unescaped != null && unescaped.trim().startsWith("{")) {
                            return unescaped;
                        }
                    }

                    // Case 3: Message är inte JSON (t.ex. confirmation / annat) -> returnera som-is
                    return msg;
                }
            }
        } catch (Exception ignored) {
            // Inte ett SNS-envelope, behandla som vanlig JSON
        }

        return body;
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
        log.debug("Deleted SQS messageId={}", message.messageId());
    }

    private String sentTimestamp(Message message) {
        return message.attributesAsStrings()
                .getOrDefault(MessageSystemAttributeName.SENT_TIMESTAMP.toString(), "unknown");
    }

    private String safeEventId(UUID id) {
        return id != null ? id.toString() : "null";
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
