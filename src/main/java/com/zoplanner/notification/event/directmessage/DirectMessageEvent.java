package com.zoplanner.notification.event.directmessage;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DirectMessageEvent(
        @JsonAlias({"eventType","EventType"}) String eventType,
        @JsonAlias({"eventId","EventId"}) UUID eventId,
        @JsonAlias({"timestamp","Timestamp"}) OffsetDateTime timestamp,
        @JsonAlias({"recipientId","RecipientId","teacherId","TeacherId"}) String recipientId,
        @JsonAlias({"recipientEmail","RecipientEmail","teacherEmail","TeacherEmail"}) String recipientEmail,
        @JsonAlias({"subject","Subject"}) String subject,
        @JsonAlias({"message","Message"}) String message,
        @JsonAlias({"createdAt","CreatedAt"}) OffsetDateTime createdAt
) {}