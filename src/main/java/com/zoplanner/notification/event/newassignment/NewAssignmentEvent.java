package com.zoplanner.notification.event.newassignment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NewAssignmentEvent(
        String eventType,
        UUID eventId,
        OffsetDateTime timestamp,
        String teacherId,
        String teacherName,
        String teacherEmail,
        String assignmentId,
        String assignmentDescription,
        LocalDate assignmentDueDate
) {
}
