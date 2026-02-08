package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class WeeklySummaryService {

    private final WeeklyEventStore weeklyEventStore;
    private final NotificationDispatcher dispatcher;

    public WeeklySummaryService(WeeklyEventStore weeklyEventStore,
                            NotificationDispatcher dispatcher) {
        this.weeklyEventStore = weeklyEventStore;
        this.dispatcher = dispatcher;
    }

//    @Scheduled(cron = "0 0 10  ? * SUN", zone = "Europe/Stockholm")
    @Scheduled(fixedDelay = 30000)
    public void sendWeeklySummaries() {


        Map<String, List<ScheduleUpdateEvent>> perTeacher =
                weeklyEventStore.drainWeeklyEvents();

        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1);

        LocalDate nextSunday = nextMonday.plusDays(6);

        perTeacher.forEach((teacherId, events) -> {

            List<ScheduleUpdateEvent> nextWeekEvents =
                    events.stream()
                            .filter(e -> {
                                LocalDate date =
                                        e.getEventTime()
                                                .atZone(ZoneId.of("Europe/Stockholm"))
                                                .toLocalDate();

                                return !date.isBefore(nextMonday)
                                        && !date.isAfter(nextSunday);
                            })
                            .toList();

            if (!nextWeekEvents.isEmpty()) {
                dispatcher.sendWeeklySummary(
                        teacherId,
                        nextWeekEvents
                );
            }
        });
    }
}
