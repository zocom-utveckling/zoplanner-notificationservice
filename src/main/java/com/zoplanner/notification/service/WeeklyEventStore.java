package com.zoplanner.notification.service;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeeklyEventStore {

    private final Map<String, List<ScheduleUpdateEvent>> eventsByTeacher
            = new ConcurrentHashMap<>();

    public void addEvent(ScheduleUpdateEvent event) {
        eventsByTeacher
                .computeIfAbsent(event.getTeacherId(), k -> new ArrayList<>())
                .add(event);
    }

    public Map<String, List<ScheduleUpdateEvent>> drainWeeklyEvents() {
        Map<String, List<ScheduleUpdateEvent>> copy =
                new HashMap<>(eventsByTeacher);
        eventsByTeacher.clear();
        return copy;
    }


}
