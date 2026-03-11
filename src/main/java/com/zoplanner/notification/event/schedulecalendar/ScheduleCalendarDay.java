package com.zoplanner.notification.event.schedulecalendar;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScheduleCalendarDay(
        @JsonAlias({"dayNumber", "DayNumber"}) String dayNumber,
        @JsonAlias({"contentHtml", "ContentHtml"}) String contentHtml
) {
}
