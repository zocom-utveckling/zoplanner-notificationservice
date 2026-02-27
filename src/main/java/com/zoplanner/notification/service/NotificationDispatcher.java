package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationDispatcher {

    public void send24hReminder(ScheduleUpdateEvent event) {
        System.out.println(
                "Reminder for " + event.getTeacherEmail() +
                        " for work at " + event.getEventTime()
        );
    }

    public void sendWeeklySummary(String email, List<ScheduleUpdateEvent> events){
        System.out.println("Weekly summary sent to " + email);
    }
    public void send(NotificationDTO notification) {
    }


}
