package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class SnsNotificationDispatcher extends NotificationDispatcher {

    private final SnsClient snsClient;
    private final String topicArn = "arn:aws:sns:eu-north-1:084828590879:Zoplanner";

    public SnsNotificationDispatcher(SnsClient snsClient) {
        super(snsClient); // fix, call parent constructor
        this.snsClient = snsClient;
    }

    @Override
    public void send24hReminder(ScheduleUpdateEvent event) {

        ZonedDateTime swedishTime =
                event.getEventTime().atZone(ZoneId.of("Europe/Stockholm"));

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Jobb påminnelse")
                .message("Hej! Du har ett jobb planerat vid " + swedishTime)
                .messageAttributes(Map.of(
                        "teacherId", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(event.getTeacherId())
                                .build()
                ))
                .build());
    }

    public void sendWeeklySummary(String teacherId, List<ScheduleUpdateEvent> events) {

        StringBuilder message = new StringBuilder("Här är din veckosammanfattning:\n\n");

        if (events.isEmpty()) {
            message.append("Inga schemalagda jobb denna vecka.");
        } else {
            message.append("Du har ")
                    .append(events.size())
                    .append(" jobb schemalagda:\n\n");

            for (ScheduleUpdateEvent event : events) {

                String when = event.getEventTime()
                        .atZone(ZoneId.of("Europe/Stockholm"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                String what =
                        event.getMessage() != null && !event.getMessage().isBlank()
                                ? event.getMessage()
                                : "Schemalagt jobb";

                message.append("- ")
                        .append(when)
                        .append(" – ")
                        .append(what)
                        .append("\n");
            }
        }

        // always publish (even if events is empty)
        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Veckoschema")
                .message(message.toString())
                .messageAttributes(Map.of(
                        "teacherId", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(teacherId)
                                .build()
                ))
                .build());
    }

    public void subscribeEmail(String email, String teacherId) {

        String filterPolicy = """
        {
          "teacherId": ["%s"]
        }
        """.formatted(teacherId);

        snsClient.subscribe(SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(email)
                .attributes(Map.of(
                        "FilterPolicy", filterPolicy
                ))
                .build());
    }
}