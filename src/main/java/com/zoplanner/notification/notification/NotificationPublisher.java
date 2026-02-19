package com.zoplanner.notification.notification;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;

public interface NotificationPublisher {
    void publishNewAssignment(NewAssignmentEvent event);
    void publishScheduleUpdate(ScheduleUpdateEvent event);
}