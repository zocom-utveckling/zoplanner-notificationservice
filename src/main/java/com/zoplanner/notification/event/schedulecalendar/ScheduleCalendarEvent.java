package com.zoplanner.notification.event.schedulecalendar;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScheduleCalendarEvent(
        @JsonAlias({"eventType", "EventType"}) String eventType,
        @JsonAlias({"teacherName", "TeacherName"}) String teacherName,
        @JsonAlias({"recipientEmail", "RecipientEmail", "teacherEmail", "TeacherEmail"}) String recipientEmail,
        @JsonAlias({"monthTitle", "MonthTitle"}) String monthTitle,
        @JsonAlias({"weekRange", "WeekRange"}) String weekRange,
        @JsonAlias({"days", "Days"}) List<ScheduleCalendarDay> days
) {
}
