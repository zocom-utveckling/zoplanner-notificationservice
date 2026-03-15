package com.zoplanner.notification.event.deleteevent;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeleteEvent(
        @JsonAlias({"eventType","EventType"})
        String eventType,

        @JsonAlias({"eventId","EventId"})
        UUID eventId,

        @JsonAlias({"timestamp","Timestamp"})
        OffsetDateTime timestamp,

        @JsonAlias({"teacherEmail","TeacherEmail"})
        String teacherEmail,

        @JsonAlias({"assignmentId","AssignmentId"})
        String assignmentId,

        @JsonAlias({"assignmentDescription","AssignmentDescription"})
        String assignmentDescription,

        @JsonAlias({"message","Message"})
        String message


) {
}

