package com.zoplanner.notification.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.notification.NotificationPublisher;
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

    public SqsPoller(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            NotificationPublisher notificationPublisher,
            @Value("${aws.sqs.queue.url:}") String queueUrl,
            @Value("${aws.sqs.polling.enabled:true}") boolean pollingEnabled,
            @Value("${aws.sqs.max.messages:10}") int maxMessages,
            @Value("${aws.sqs.wait.time.seconds:20}") int waitTimeSeconds
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
        this.pollingEnabled = pollingEnabled;
        this.notificationPublisher = notificationPublisher;
        this.maxMessages = maxMessages;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.polling.delay.ms:5000}")
    public void poll() {
        if (!pollingEnabled) {
            log.debug("SQS polling is disabled (aws.sqs.polling.enabled=false).");
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
        try {
            NewAssignmentEvent event = objectMapper.readValue(message.body(), NewAssignmentEvent.class);
            log.info(
                    "Received SQS messageId={} SentTs={} eventType={} eventId={}",
                    message.messageId(),
                    sentTimestamp(message),
                    event.eventType(),
                    safeEventId(event)
            );

            if (event.eventType() == null || !event.eventType().equalsIgnoreCase("NEW_ASSIGNMENT")) {
                log.warn("Ignoring messageId={} due to unsupported eventType={}", message.messageId(), event.eventType());
                // deleteMessage(message); ta bort "//" om meddelande ska tas bort och inte felsökas
                return;
            }

            // 1. Publish to SNS
            notificationPublisher.publishNewAssignment(event);

            // 2. Delete only if publish successfull
            deleteMessage(message);

        } catch (Exception e) {
            log.error("Failed processing SQS messageId={} (will retry later). Body={}", message.messageId(), message.body(), e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());

        log.debug("Deleted SQS messageId={}", message.messageId());
    }

    private String safeEventId(NewAssignmentEvent event) {
        UUID id = event.eventId();
        return id != null ? id.toString() : "null";
    }

    private String sentTimestamp(Message message) {
        return message.attributesAsStrings()
                .getOrDefault(MessageSystemAttributeName.SENT_TIMESTAMP.toString(), "unknown");
    }



}
