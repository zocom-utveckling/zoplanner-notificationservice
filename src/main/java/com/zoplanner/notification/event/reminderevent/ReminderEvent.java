package com.zoplanner.notification.event.reminderevent;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ReminderEvent(


        @JsonAlias({"EventType", "eventType"})
        String eventType,
        @JsonAlias({"TeacherEmail", "teacherEmail"})
        String teacherEmail,
        @JsonAlias({"Message", "message"})
        String message,
        @JsonAlias({"SendAt", "sendAt"})
        LocalDateTime sendAt,
        @JsonAlias({"EventTime", "eventTime"})
        LocalDateTime eventDate

) {}