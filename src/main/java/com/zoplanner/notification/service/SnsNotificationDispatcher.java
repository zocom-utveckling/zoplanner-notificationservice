package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;


import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class SnsNotificationDispatcher extends NotificationDispatcher {

    private final SnsClient snsClient;
    private final String topicArn = "arn:aws:sns:eu-north-1:084828590879:Zoplanner";



    public SnsNotificationDispatcher( SnsClient snsClient ) {
        this.snsClient = snsClient;
    }



    @Override
    public void send24hReminder(ScheduleUpdateEvent event) {

        System.out.println("📤 SKICKAR SNS MAIL TILL teacherId=" + event.getTeacherId());

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject("TEST – Schema notis")
                .message("TEST: Jobb planerat " + event.getEventTime())
                .messageAttributes(Map.of(
                        "teacherId", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(event.getTeacherId())
                                .build()
                ))
                .build());

//        snsClient.publish(PublishRequest.builder()
//                .topicArn(topicArn)
//                .subject("Jobb påminnelse")
//                .message("Hej! Du har ett jobb planerat vid " + event.getEventTime())
//                .messageAttributes(Map.of(
//                        "teacherId", MessageAttributeValue.builder()
//                                .dataType("String")
//                                .stringValue(event.getTeacherId())
//                                .build()
//                ))
//                .build());
    }

    public void sendWeeklySummary(String teacherId, List<ScheduleUpdateEvent> events) {
        StringBuilder message = new StringBuilder("Här är din veckosammanfattning:\n\n");

        if (events.isEmpty()) {
            message.append("Inga schemalagda jobb denna vecka.");
        } else {
            message.append("Du har ").append(events.size()).append(" jobb schemalagda:\n\n");

            events.forEach(event -> {
                message.append("- ")
                        .append(event.getEventTime().atZone(ZoneId.of("Europe/Stockholm"))
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .append("\n");
            });
        }

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



    @Override
    public void send(NotificationDTO notification) {

        System.out.println("📤 SKICKAR SNS MAIL TILL teacherId=" + notification.getTeacherId());

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .messageAttributes(Map.of(
                        "teacherId", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(notification.getTeacherId())
                                .build()
                ))
                .build());
    }
}
