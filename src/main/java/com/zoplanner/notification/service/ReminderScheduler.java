package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.Reminder;
import com.zoplanner.notification.notification.NotificationPublisher;
import com.zoplanner.notification.notification.SesNotificationPublisher;
import com.zoplanner.notification.repository.ReminderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ReminderScheduler {
    private final ReminderRepository repository;
    private final SesNotificationPublisher publisher;

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(2);

    public ReminderScheduler(ReminderRepository repository, SesNotificationPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }




    @Scheduled(fixedRate = 60000)
    public void sendReminders() {
        log.info("Checking for reminders to send...");

        List<Reminder> reminders =
                repository.findBySendAtBeforeAndStatus(LocalDateTime.now(), "PENDING");

        for (Reminder r : reminders) {

            try {
                publisher.publishReminder(r);

                r.setStatus("SENT");

            } catch (Exception e) {

                log.error("Failed to send reminder id={}", r.getId(), e);

                r.setAttempts(r.getAttempts() + 1);
                r.setLastError(e.getMessage());

                if (r.getAttempts() >= 3) {

                    r.setStatus("FAILED");

                } else {

                    r.setSendAt(LocalDateTime.now().plusMinutes(5));
                }
            }

            repository.save(r);
        }
    }

    public void scheduleReminder(Instant runAt, Runnable task){

        long delay = Duration.between(Instant.now(),runAt).toMillis();

        if (delay <= 0){
            task.run();

        }else {
            executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        }
    }

}
