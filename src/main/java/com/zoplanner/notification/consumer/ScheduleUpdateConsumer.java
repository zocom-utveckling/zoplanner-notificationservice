package com.zoplanner.notification.consumer;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.ReminderScheduler;
import com.zoplanner.notification.service.WeeklyEventStore;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ScheduleUpdateConsumer {
    private final ReminderScheduler scheduler;
    private final NotificationDispatcher dispatcher;
    private final WeeklyEventStore weeklyEventStore;

    private final ConcurrentHashMap<String, List<ScheduleUpdateEvent>> weeklyEvent =
            new ConcurrentHashMap<>();

    public ScheduleUpdateConsumer(ReminderScheduler scheduler,
                                  NotificationDispatcher dispatcher, WeeklyEventStore weeklyEventStore) {
        this.scheduler = scheduler;
        this.dispatcher = dispatcher;
        this.weeklyEventStore = weeklyEventStore;
    }

    public void handleMessage(ScheduleUpdateEvent event){

        if (event.getPreference() == NotificationPreference.PER_JOB_24H){
            schedule24Reminder(event);
        }
        if (event.getPreference() == NotificationPreference.WEEKLY_SUMMARY){
            if (weeklyEventStore != null) {
                weeklyEventStore.addEvent(event);
            } else {
                weeklyEvent.put(event.getTeacherEmail(), List.of());
            }
        }
    }

    private void schedule24Reminder(ScheduleUpdateEvent event){
        log.info("24h flow triggered for event {}", event.getEventTime().atZone(ZoneId.of("Europe/Stockholm")));

        Instant reminderTime = event.getEventTime().minus(24, ChronoUnit.HOURS);

        scheduler.scheduleReminder(
                reminderTime,
                ()-> dispatcher.send24hReminder(event)

        );
    }

    @Scheduled(cron = "0 0 12 ? * SUN")
    public void sendWeeklySummaries(){
        weeklyEvent.forEach((teacherId, events) -> {
            dispatcher.sendWeeklySummary(teacherId, List.of());
        });
        weeklyEvent.clear();
    }

}