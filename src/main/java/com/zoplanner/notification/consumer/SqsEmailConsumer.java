package com.zoplanner.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.BaseEvent;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.handler.NewAssignmentNotificationHandler;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.NotificationService;
import com.zoplanner.notification.service.WeeklyEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

// @Component
public class SqsEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsEmailConsumer.class);

    private final SqsClient sqsClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final NewAssignmentNotificationHandler newAssignmentHandler;
    private  NotificationDispatcher notificationDispatcher;
    private  WeeklyEventStore weeklyEventStore;
    private ScheduleUpdateConsumer updateConsumer;

    @Value("${aws.sqs.queue.url}")
    private String queueUrl;

    @Value("${notification.sqs.enabled:false}")
    private boolean pollingEnabled;

    /**
     * Tests override this via ReflectionTestUtils.setField(consumer, "maxMessages", ...)
     */
    private int maxMessages = 10;

    /**
     * Tests override this via ReflectionTestUtils.setField(consumer, "waitTimeSeconds", ...)
     */
    private int waitTimeSeconds = 10;

    // Order matches tests: (SqsClient, NotificationService, ObjectMapper)
    public SqsEmailConsumer(SqsClient sqsClient,
                            NotificationService notificationService,
                            ObjectMapper objectMapper,
                            NewAssignmentNotificationHandler newAssignmentHandler, ScheduleUpdateConsumer updateConsumer
    ) {
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
            log.error("Error polling messages from SQS", e);
        }
    }

    void processMessage(Message message) {
        try {
            BaseEvent baseEvent =
                    objectMapper.readValue(message.body(), BaseEvent.class);

            log.debug("Processing message {} eventType={}",
                    message.messageId(), baseEvent.eventType());

            if (!StringUtils.hasText(baseEvent.eventType())) {
                log.warn("❌ Saknar eventType – ignorerar message {}", message.messageId());
                return;
            }

            NotificationDTO notificationDTO =
                    objectMapper.readValue(message.body(), NotificationDTO.class);


            switch (baseEvent.eventType()) {

                case "NEW_ASSIGNMENT" -> {
                    handleNewAssignment(message.body());
                    deleteMessage(message);
                }

                case "LEGACY_EMAIL" -> {
                    boolean processed = handleLegacyNotification(message);
                    if (processed) {
                        deleteMessage(message);
                    }
                }

                case "SCHEDULE_UPDATED" -> {
                    ScheduleUpdateEvent event =
                            objectMapper.readValue(message.body(), ScheduleUpdateEvent.class);

                    updateConsumer.handleMessage(event);

                    deleteMessage(message);
                }


                default -> {
                    log.warn("⚠️ Okänt eventType {}, ignorerar", baseEvent.eventType());

                }
            }

        } catch (Exception e) {
            log.error("Error processing message from SQS", e);
        }
        try {
            BaseEvent baseEvent =
                    objectMapper.readValue(message.body(), BaseEvent.class);

            log.debug("Processing SQS messageId {} with eventType {}",
                    message.messageId(),
                    baseEvent.eventType());

            if (!StringUtils.hasText(baseEvent.eventType())) {
                log.debug("Missing eventType for message {}, treating as legacy notification",
                        message.messageId());
                boolean processed = handleLegacyNotification(message);
                if (processed) {
                    deleteMessage(message);
                }
                return;
            }

            switch (baseEvent.eventType()) {
                case "NEW_ASSIGNMENT" -> handleNewAssignment(message.body());
                default -> {
                    boolean processed = handleLegacyNotification(message);
                    if (processed) {
                        deleteMessage(message);
                    }
                    return;
                }
            }

            deleteMessage(message);

        } catch (Exception e) {
            log.error("Error processing message from SQS", e);
        }
    }

    void deleteMessage(Message message) {
        try {
            DeleteMessageRequest delete = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(delete);
            log.debug("Deleted SQS message {}", message.messageId());
        } catch (Exception e) {
            log.error("Failed to delete SQS message {}", message.messageId(), e);
        }
    }

    private boolean handleLegacyNotification(Message message) throws JsonProcessingException {
        NotificationDTO dto =
                objectMapper.readValue(message.body(),
                        NotificationDTO.class);

        if (!StringUtils.hasText(dto.getRecipient())) {
            log.warn("Skipping message with missing recipient: {}", message.messageId());
            return false;
        }

        if (!StringUtils.hasText(dto.getMessage()) && !StringUtils.hasText(dto.getEmailBody())) {
            log.warn("Skipping message with missing message/email body: {}", message.messageId());
            return false;
        }

        log.debug("Handling legacy notification for recipient {}",
                dto.getRecipient());

        notificationService.createNotification(dto);
        return true;
    }

    private void handleNewAssignment(String messageBody) throws JsonProcessingException {
        NewAssignmentEvent event = objectMapper.readValue(messageBody,
                NewAssignmentEvent.class);

        log.info("Handling NEW_ASSIGNMENT event for teacherEmail {}",
                event.teacherEmail());

        newAssignmentHandler.handle(event);
    }

    // setters for tests (in addition to ReflectionTestUtils)
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