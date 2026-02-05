package com.zoplanner.notification.consumer;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.ReminderScheduler;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduleUpdateConsumer {
    private final ReminderScheduler scheduler;
    private final NotificationDispatcher dispatcher;


    private final ConcurrentHashMap<String, List<ScheduleUpdateEvent>> weeklyEvent =
            new ConcurrentHashMap<>();

    public ScheduleUpdateConsumer(ReminderScheduler scheduler,
                                  NotificationDispatcher dispatcher) {
        this.scheduler = scheduler;
        this.dispatcher = dispatcher;
    }
    @SqsListener("zoplanner-notification-queue")
    public void handleMessage(ScheduleUpdateEvent event){


        if (event.getPreference() == NotificationPreference.PER_JOB_24H){
            schedule24Reminder(event);
        }
        if (event.getPreference() == NotificationPreference.WEEKLY_SUMMARY){
            weeklyEvent.computeIfAbsent(
                    event.getTeacherId(),
                    k -> new java.util.concurrent.CopyOnWriteArrayList<>()

            ).add(event);
        }



    }
private void schedule24Reminder(ScheduleUpdateEvent event){
    Instant reminderTime = event.getEventTime().minus(24, ChronoUnit.HOURS);
    scheduler.scheduleReminder(
            reminderTime,
            ()-> dispatcher.send24hReminder(event)
    );
}

    @Scheduled(cron = "0 0 7 ? * MON")
    public void sendWeeklySummaries(){
        weeklyEvent.forEach((teacherId, events) -> {
            if (!events.isEmpty()){
                dispatcher.sendWeeklySummary(teacherId,events);
            }
        });
        weeklyEvent.clear();
    }

}
