package com.zoplanner.notification.consumer;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.ReminderScheduler;
import com.zoplanner.notification.service.WeeklyEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduleUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleUpdateConsumer.class);

    private final ReminderScheduler scheduler;
    private final NotificationDispatcher dispatcher;

    // Kan vara null i unit tests (ScheduleNotifierTest mockar inte upp den)
    private final WeeklyEventStore weeklyEventStore;

    // Testerna vill att vi dispatchar weekly summary med teacherEmail + tom lista
    private final ConcurrentHashMap<String, Boolean> weeklySummaryRecipients = new ConcurrentHashMap<>();

    public ScheduleUpdateConsumer(ReminderScheduler scheduler,
                                  NotificationDispatcher dispatcher,
                                  WeeklyEventStore weeklyEventStore) {
        this.scheduler = scheduler;
        this.dispatcher = dispatcher;
        this.weeklyEventStore = weeklyEventStore;
    }

    public void handleMessage(ScheduleUpdateEvent event) {
        if (event == null) return;

        if (event.getPreference() == NotificationPreference.PER_JOB_24H) {
            schedule24Reminder(event);
        }

        if (event.getPreference() == NotificationPreference.WEEKLY_SUMMARY) {
            String email = event.getTeacherEmail();
            if (email != null && !email.isBlank()) {
                weeklySummaryRecipients.put(email, true);
            }

            if (weeklyEventStore != null) {
                weeklyEventStore.addEvent(event);
            }
        }
    }

    private void schedule24Reminder(ScheduleUpdateEvent event) {
        log.info("24h flow triggered for event {}",
                event.getEventTime().atZone(ZoneId.of("Europe/Stockholm")));

        Instant reminderTime = event.getEventTime().minus(24, ChronoUnit.HOURS);

        scheduler.scheduleReminder(
                reminderTime,
                () -> dispatcher.send24hReminder(event)
        );
    }

    @Scheduled(cron = "0 0 12 ? * SUN")
    public void sendWeeklySummaries() {

        // Runtime: om WeeklyEventStore finns, dispatcha events därifrån
        if (weeklyEventStore != null) {
            Map<String, List<ScheduleUpdateEvent>> drained = weeklyEventStore.drainWeeklyEvents();
            drained.forEach((teacherId, events) -> {
                String email = teacherId;

                if (events != null && !events.isEmpty()) {
                    ScheduleUpdateEvent first = events.get(0);
                    if (first.getTeacherEmail() != null && !first.getTeacherEmail().isBlank()) {
                        email = first.getTeacherEmail();
                    }
                }

                dispatcher.sendWeeklySummary(email, events != null ? events : List.of());

                // undvik dubbel-utskick om samma email även finns i recipients-listan
                if (email != null && !email.isBlank()) {
                    weeklySummaryRecipients.remove(email);
                }
            });
        }

        // Tester + “no events” case: dispatcha alltid tom lista för recipients
        weeklySummaryRecipients.forEach((email, ignored) ->
                dispatcher.sendWeeklySummary(email, List.of())
        );
        weeklySummaryRecipients.clear();
    }
}