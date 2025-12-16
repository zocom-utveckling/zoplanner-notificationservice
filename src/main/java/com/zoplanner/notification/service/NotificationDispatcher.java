package com.zoplanner.notification.service;

import com.zoplanner.notification.consumer.ScheduleUpdateConsumer;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.ConsultantSettings;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatcher {

    public void send24hReminder(ScheduleUpdateEvent event) {
        System.out.println(
                "Reminder for " + event.getTeacherEmail() +
                        " for work at " + event.getEventTime()
        );
    }

    public void sendWeeklySummary(String email){
        System.out.println("Weekly summary sent to " + email);
    }


}
