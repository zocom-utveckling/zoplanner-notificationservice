package com.zoplanner.notification.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "notification.publisher", havingValue = "sns", matchIfMissing = true)
public class SnsNotificationPublisher implements NotificationPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String emailTopicArn;
    private final String eventsTopicArn;

    @SuppressWarnings("unused")
    private final MessageSource messageSource;

    public SnsNotificationPublisher(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sns.topic.email}") String emailTopicArn,
            @Value("${aws.sns.topic.events}") String eventsTopicArn,
            MessageSource messageSource
    ) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.emailTopicArn = emailTopicArn;
        this.eventsTopicArn = eventsTopicArn;
        this.messageSource = messageSource;

        log.info("SNSNotificationPublisher initialized (emailTopicArn={}, eventsTopicArn={}).",
                emailTopicArn, eventsTopicArn);
    }

    @Override
    public void publishNewAssignment(NewAssignmentEvent event) {
        requireEmailTopicArn();

        String message = buildEmailMessage(event);

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
                .topicArn(emailTopicArn)
                .subject("NEW_ASSIGNMENT")
                .message(message)
                .messageAttributes(attrs)
                .build();

        PublishResponse response = snsClient.publish(request);
        log.info("Published NEW_ASSIGNMENT to SNS email topic. messageId={} topicArn={}", response.messageId(), emailTopicArn);

        // Optional: if you also want the machine event on the events topic, uncomment:
        // publishEventJson(eventsTopicArn, "NEW_ASSIGNMENT", event, attrs);
    }

    @Override
    public void publishScheduleUpdate(ScheduleUpdateEvent event) {
        requireEmailTopicArn();

        String teacherId = event.getTeacherId();
        if (teacherId == null || teacherId.isBlank()) {
            log.warn("Missing teacherId on SCHEDULE_UPDATED event. Not publishing to SNS. event={}", event);
            return;
        }

        String eventType = "SCHEDULE_UPDATED";
        String message = buildEmailMessage(event);

        Map<String, MessageAttributeValue> attrs = Map.of(
                "teacherId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(safe(event.getTeacherId()))
                        .build(),
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(eventType)
                        .build(),
                "preference", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(event.getPreference() != null ? event.getPreference().name() : "-")
                        .build()
        );

        PublishRequest request = PublishRequest.builder()
                .topicArn(emailTopicArn)
                .subject(eventType)
                .message(message)
                .messageAttributes(attrs)
                .build();

        PublishResponse response = snsClient.publish(request);
        log.info("Published {} to SNS email topic. messageId={} topicArn={}", eventType, response.messageId(), emailTopicArn);

        // Optional: if you also want the machine event on the events topic, uncomment:
        // publishEventJson(eventsTopicArn, eventType, event, attrs);
    }

    @SuppressWarnings("unused")
    private void publishEventJson(String subject, Object payload, Map<String, MessageAttributeValue> attrs) {
        requireEventsTopicArn();

        String json = toJson(payload);

        PublishRequest request = PublishRequest.builder()
                .topicArn(eventsTopicArn)
                .subject(subject)
                .message(json)
                .messageAttributes(attrs)
                .build();

        PublishResponse response = snsClient.publish(request);
        log.info("Published {} to SNS events topic. messageId={} topicArn={}", subject, response.messageId(), eventsTopicArn);
    }

    private void requireEmailTopicArn() {
        if (emailTopicArn == null || emailTopicArn.isBlank()) {
            throw new IllegalStateException("Email Topic ARN is empty. Check aws.sns.topic.email or SNS_TOPIC_EMAIL_ARN.");
        }
    }

    private void requireEventsTopicArn() {
        if (eventsTopicArn == null || eventsTopicArn.isBlank()) {
            throw new IllegalStateException("Events Topic ARN is empty. Check aws.sns.topic.events or SNS_TOPIC_EVENTS_ARN.");
        }
    }



    private String buildEmailMessage(NewAssignmentEvent e) {
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

    private String buildEmailMessage(ScheduleUpdateEvent e) {
        int changeCount = (e.getChanges() == null) ? 0 : e.getChanges().size();

        return """
                Ditt schema har uppdaterats.
                Lärare (email): %s
                Källa: %s
                Preferens: %s
                Tid för händelse: %s
                Skapad: %s
                Antal ändringar: %s
                
                Meddelande:
                %s
                """.formatted(
                safe(e.getTeacherEmail()),
                safe(e.getSource()),
                e.getPreference() != null ? e.getPreference().name() : "-",
                e.getEventTime() != null ? e.getEventTime().toString() : "-",
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : "-",
                Integer.toString(changeCount),
                safe(e.getMessage())
        );

    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    @SuppressWarnings("unused")
    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize event to JSON", ex);
        }
    }
}
