package com.zoplanner.notification.event.newassignment;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewAssignmentEvent(
        @JsonAlias({"eventType","EventType"}) String eventType,
        @JsonAlias({"eventId","EventId"}) UUID eventId,
        @JsonAlias({"timestamp","Timestamp"}) OffsetDateTime timestamp,
        @JsonAlias({"teacherId","TeacherId"}) String teacherId,
        @JsonAlias({"teacherName","TeacherName"}) String teacherName,
        @JsonAlias({"teacherEmail","TeacherEmail"}) String teacherEmail,
        @JsonAlias({"assignmentId","AssignmentId"}) String assignmentId,
        @JsonAlias({"assignmentDescription","AssignmentDescription"}) String assignmentDescription,
        @JsonAlias({"assignmentDueDate","AssignmentDueDate"}) LocalDate assignmentDueDate
) {
}
