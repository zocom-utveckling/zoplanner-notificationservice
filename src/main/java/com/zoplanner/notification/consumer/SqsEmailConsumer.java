package com.zoplanner.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.BaseEvent;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.handler.NewAssignmentNotificationHandler;
import com.zoplanner.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class SqsEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsEmailConsumer.class);

    private final SqsClient sqsClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final NewAssignmentNotificationHandler newAssignmentHandler;
    private final ScheduleUpdateConsumer updateConsumer;

    @Value("${aws.sqs.queue.url:}")
    private String queueUrl;

    @Value("${notification.sqs.enabled:false}")
    private boolean pollingEnabled;

    private int maxMessages = 10;
    private int waitTimeSeconds = 10;

    // ✅ Constructor-signatur som dina tester använder:
    public SqsEmailConsumer(SqsClient sqsClient,
                            NotificationService notificationService,
                            ObjectMapper objectMapper,
                            NewAssignmentNotificationHandler newAssignmentHandler,
                            ScheduleUpdateConsumer updateConsumer) {
        this.sqsClient = sqsClient;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.newAssignmentHandler = newAssignmentHandler;
        this.updateConsumer = updateConsumer;
    }

    @Scheduled(fixedDelayString = "${notification.sqs.pollDelayMs:10000}")
    public void pollMessages() {
        if (!pollingEnabled) {
            log.debug("SQS polling is disabled, skipping poll.");
            return;
        }

        if (!StringUtils.hasText(queueUrl)) {
            log.warn("SQS queue URL is empty, skipping poll.");
            return;
        }

        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();

            if (messages == null || messages.isEmpty()) {
                log.debug("No messages received from SQS.");
                return;
            }

            for (Message message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            // Test vill att vi inte kastar här
            log.error("Error polling messages from SQS", e);
        }
    }

    void processMessage(Message message) {
        try {
            String body = message.body();

            // Först: försök läsa eventType (om det finns)
            BaseEvent baseEvent = objectMapper.readValue(body, BaseEvent.class);
            String eventType = baseEvent.eventType();

            // ✅ Legacy: saknar eventType -> tolka som NotificationDTO och skapa notification
            if (!StringUtils.hasText(eventType)) {
                handleLegacyNotificationAndMaybeDelete(message);
                return;
            }

            switch (eventType) {
                case "NEW_ASSIGNMENT" -> {
                    NewAssignmentEvent event = objectMapper.readValue(body, NewAssignmentEvent.class);
                    log.info("Handling NEW_ASSIGNMENT event for teacherEmail {}", event.teacherEmail());

                    // Om handler kastar -> outer catch -> delete INTE (som test kräver)
                    newAssignmentHandler.handle(event);

                    deleteMessage(message);
                }

                case "SCHEDULE_UPDATED" -> {
                    if (updateConsumer != null) {
                        ScheduleUpdateEvent event = objectMapper.readValue(body, ScheduleUpdateEvent.class);
                        updateConsumer.handleMessage(event);
                    }
                    deleteMessage(message);
                }

                default -> {
                    // Default: behandla som legacy NotificationDTO
                    handleLegacyNotificationAndMaybeDelete(message);
                }
            }

        } catch (Exception e) {
            // Vid fel ska vi INTE delete:a (tester förväntar sig det)
            log.error("Error processing message from SQS", e);
        }
    }

    private void handleLegacyNotificationAndMaybeDelete(Message message) throws JsonProcessingException {
        NotificationDTO dto = objectMapper.readValue(message.body(), NotificationDTO.class);

        // Test: Missing recipient -> inga calls + ingen delete
        if (!StringUtils.hasText(dto.getRecipient())) {
            log.warn("Skipping message with missing recipient: {}", message.messageId());
            return;
        }

        // Test: kräver att vi inte skapar notification om både message + emailBody saknas
        boolean hasTextMessage = StringUtils.hasText(dto.getMessage());
        boolean hasEmailBody = StringUtils.hasText(dto.getEmailBody());

        if (!hasTextMessage && !hasEmailBody) {
            log.warn("Skipping message with missing message/email body: {}", message.messageId());
            return;
        }

        // Test: vill att createNotification kallas (och om den kastar -> ingen delete)
        notificationService.createNotification(dto);

        deleteMessage(message);
    }

    void deleteMessage(Message message) {
        try {
            DeleteMessageResponse resp = sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
            log.debug("Deleted SQS message {}", message.messageId());
        } catch (Exception e) {
            log.error("Failed to delete SQS message {}", message.messageId(), e);
        }
    }

    // setters (för tester / debug)
    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public void setPollingEnabled(boolean pollingEnabled) {
        this.pollingEnabled = pollingEnabled;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public void setWaitTimeSeconds(int waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }
}