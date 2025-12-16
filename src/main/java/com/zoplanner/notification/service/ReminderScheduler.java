package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ReminderScheduler {

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(2);

    public void scheduleReminder(Instant runAt, Runnable task){

        long delay = Duration.between(Instant.now(),runAt).toMillis();

        if (delay <= 0){
            task.run();

        }else {
            executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        }
    }


}
