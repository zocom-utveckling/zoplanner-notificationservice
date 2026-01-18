package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;


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

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Jobb påminnelse")
                .message("Hej! Du har ett jobb planerat vid " + event.getEventTime())
                .messageAttributes(Map.of(
                        "teacherId", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(event.getTeacherId())
                                .build()
                ))
                .build());
    }

    public void sendWeeklySummary(String teacherId) {

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Veckoschema")
                .message("Här är din veckosammanfattning")
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
