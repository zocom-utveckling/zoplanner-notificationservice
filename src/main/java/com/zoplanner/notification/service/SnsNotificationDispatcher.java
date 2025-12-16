package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
@Primary
public class SnsNotificationDispatcher extends NotificationDispatcher {

    private final SnsClient snsClient;
    private String topicArn = "arn:aws:sns:eu-north-1:000000000000:test-topic";

    public SnsNotificationDispatcher( SnsClient snsClient ) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }


    @Override
    public void send24hReminder(ScheduleUpdateEvent event) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Jobb påminnelse")
                .message("Hej! Du har ett jobb planerat vid " + event.getEventTime())
                .build();

        snsClient.publish(request);
    }
    @Override
    public void sendWeeklySummary(String email) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Veckoschema")
                .message("Här är din veckosammanfattning")
                .build();

        snsClient.publish(request);
    }

}
