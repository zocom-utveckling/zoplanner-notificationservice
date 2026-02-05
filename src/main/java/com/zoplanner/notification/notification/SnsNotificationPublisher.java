package com.zoplanner.notification.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Slf4j
@Component
public class SnsNotificationPublisher implements NotificationPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsNotificationPublisher(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sns.topic.email}") String topicArn
    ) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    @Override
    public void publishNewAssignment(NewAssignmentEvent event) {
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException("Topic ARN is empty. Check aws.sns.topic.email.");
        }

        String payload = toJson(event);

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("NEW_ASSIGNMENT")
                .message(payload)
                .build();

        PublishResponse response = snsClient.publish(request);

        log.info("Published NEW_ASSIGNMENT to SNS. messageId: {} topicArn: {}", response.messageId(), topicArn);
    }

    private String toJson(NewAssignmentEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event to JSON for publish", e);
        }
    }
}
