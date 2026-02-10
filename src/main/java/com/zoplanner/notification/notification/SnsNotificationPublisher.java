package com.zoplanner.notification.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import java.util.Map;

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

        String message = buildMessage(event);

        Map<String, MessageAttributeValue> attrs = Map.of(
                "teacherId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.teacherId())
                        .build(),
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.eventType())
                        .build()
        );

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("NEW_ASSIGNMENT")
                .message(message)
                .messageAttributes(attrs)
                .build();

        PublishResponse response = snsClient.publish(request);

        log.info("Published NEW_ASSIGNMENT to SNS. messageId: {} topicArn: {}", response.messageId(), topicArn);
    }

    private String buildMessage(NewAssignmentEvent e) {
        return """
                Du har fått en ny uppgift:

                Lärare: %s
                Beskrivning: %s
                Deadline: %s
                """.formatted(
                safe(e.teacherName()),
                safe(e.assignmentDescription()),
                e.assignmentDueDate() != null ? e.assignmentDueDate().toString() : "-"
        );
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    @SuppressWarnings("unused")
    private String toJson(NewAssignmentEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize event to JSON", ex);
        }
    }
}
