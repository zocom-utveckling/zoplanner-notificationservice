package com.zoplanner.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

/**
 * SQS Email Consumer
 * Lyssnar på SQS-kön och triggar e-postutskick när meddelanden kommer in
 * Använder scheduled polling för att regelbundet hämta meddelanden från kön
 */
@Slf4j
@Component
public class SqsEmailConsumer {

    private final SqsClient sqsClient;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue.url}")
    private String queueUrl;

    @Value("${aws.sqs.polling.enabled:true}")
    private boolean pollingEnabled;

    @Value("${aws.sqs.max.messages:10}")
    private int maxMessages;

    @Value("${aws.sqs.wait.time.seconds:20}")
    private int waitTimeSeconds;

    public SqsEmailConsumer(SqsClient sqsClient, NotificationService notificationService, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Pollar SQS-kön varje 5 sekunder för nya meddelanden
     * Long polling används (waitTimeSeconds) för att minska kostnader och förbättra prestanda
     */
    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        if (!pollingEnabled) {
            log.debug("SQS polling is disabled");
            return;
        }

        if (queueUrl == null || queueUrl.isEmpty()) {
            log.warn("SQS queue URL is not configured. Skipping polling.");
            return;
        }

        try {
            log.debug("Polling SQS queue: {}", queueUrl);

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (!messages.isEmpty()) {
                log.info("Received {} message(s) from SQS queue", messages.size());
                messages.forEach(this::processMessage);
            } else {
                log.debug("No messages received from SQS queue");
            }

        } catch (SqsException e) {
            String errorMessage = e.awsErrorDetails() != null
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();
            log.error("Error receiving messages from SQS: {}", errorMessage, e);
        } catch (Exception e) {
            log.error("Unexpected error while polling SQS queue", e);
        }
    }

    /**
     * Bearbetar ett enskilt SQS-meddelande
     * Parsar JSON till NotificationDTO och triggar e-postutskick
     *
     * @param message SQS-meddelandet att bearbeta
     */
    private void processMessage(Message message) {
        String messageId = message.messageId();
        String receiptHandle = message.receiptHandle();

        log.info("Processing SQS message. MessageId: {}", messageId);
        log.debug("Message body: {}", message.body());

        try {
            NotificationDTO notificationDTO = objectMapper.readValue(message.body(), NotificationDTO.class);
            log.debug("Parsed notification DTO: {}", notificationDTO);

            validateNotificationDTO(notificationDTO);

            notificationService.createNotification(notificationDTO);

            deleteMessage(receiptHandle, messageId);

            log.info("Successfully processed and deleted SQS message. MessageId: {}", messageId);

        } catch (Exception e) {
            log.error("Failed to process SQS message. MessageId: {}. Error: {}", messageId, e.getMessage(), e);
            // Meddelandet kommer att återgå till kön efter visibility timeout
            // Överväg att implementera Dead Letter Queue (DLQ) för meddelanden som misslyckas upprepade gånger
        }
    }

    /**
     * Validerar att NotificationDTO innehåller nödvändiga fält
     *
     * @param dto NotificationDTO att validera
     * @throws IllegalArgumentException om obligatoriska fält saknas
     */
    private void validateNotificationDTO(NotificationDTO dto) {
        if (dto.getRecipient() == null || dto.getRecipient().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }

        if (dto.getMessage() == null || dto.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }

        boolean hasEmailFields = dto.getSubject() != null || dto.getEmailBody() != null;
        if (hasEmailFields) {
            if (dto.getSubject() == null || dto.getSubject().isEmpty()) {
                throw new IllegalArgumentException("Email subject is required when sending email");
            }
            if (dto.getEmailBody() == null || dto.getEmailBody().isEmpty()) {
                throw new IllegalArgumentException("Email body is required when sending email");
            }
        }
    }

    /**
     * Tar bort ett meddelande från SQS-kön efter framgångsrik bearbetning
     *
     * @param receiptHandle Receipt handle för meddelandet
     * @param messageId MessageId för loggning
     */
    private void deleteMessage(String receiptHandle, String messageId) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteRequest);
            log.debug("Deleted message from queue. MessageId: {}", messageId);

        } catch (SqsException e) {
            String errorMessage = e.awsErrorDetails() != null
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();
            log.error("Failed to delete message from SQS. MessageId: {}. Error: {}",
                    messageId, errorMessage, e);
        }
    }
}

