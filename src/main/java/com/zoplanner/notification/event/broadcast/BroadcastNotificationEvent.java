package com.zoplanner.notification.event.broadcast;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zoplanner.notification.dto.EmailType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BroadcastNotificationEvent(
        @JsonAlias({"eventType","EventType"}) String eventType,
        @JsonAlias({"eventId","EventId"}) UUID eventId,
        @JsonAlias({"timestamp","Timestamp"}) OffsetDateTime timestamp,

        @JsonAlias({"managerId","ManagerId"}) String managerId,
        @JsonAlias({"recipientGroup","RecipientGroup"}) String recipientGroup, // ex. "AllMyConsultants"
        @JsonAlias({"recipientEmails","RecipientEmails"}) List<String> recipientEmails,

        @JsonAlias({"subject","Subject"}) String subject,
        @JsonAlias({"message","Message"}) String message,
        @JsonAlias({"emailType","EmailType"}) EmailType emailType
) {
}