package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.List;

@Service
public class NotificationDispatcher {

    private final SnsClient snsClient;

    @Value("${aws.sns.topic.arn:}")
    private String topicArn;

    public NotificationDispatcher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void send24hReminder(ScheduleUpdateEvent event) {
        System.out.println(
                "Reminder for " + event.getTeacherEmail() +
                        " for work at " + event.getEventTime()
        );
    }

    public void sendWeeklySummary(String email, List<ScheduleUpdateEvent> events){
        // tests expect we publish even if events is empty.
        String message = "Weekly summary for " + email;

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .subject("Weekly Summary")
                .build();

        snsClient.publish(request);
    }

    public void send(NotificationDTO notification) {
    }
}