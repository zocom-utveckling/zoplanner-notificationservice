package com.zoplanner.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.service.NotificationService;
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

@Component
public class SqsEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsEmailConsumer.class);

    private final SqsClient sqsClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${notification.sqs.queueUrl:}")
    private String queueUrl;

    @Value("${notification.sqs.enabled:true}")
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
                            ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
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
            NotificationDTO dto = objectMapper.readValue(message.body(), NotificationDTO.class);

            if (!StringUtils.hasText(dto.getRecipient())) {
                log.warn("Skipping message because recipient is missing: {}", message.messageId());
                return;
            }

            if (!StringUtils.hasText(dto.getMessage()) && !StringUtils.hasText(dto.getEmailBody())) {
                log.warn("Skipping message because message/email body is missing: {}", message.messageId());
                return;
            }

            notificationService.createNotification(dto);
            deleteMessage(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize SQS message body: {}", message.body(), e);
        } catch (Exception e) {
            // If createNotification fails, we should NOT delete the message
            log.error("Error processing SQS message {}", message.messageId(), e);
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
