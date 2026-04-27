package com.zoplanner.notification.notification.email;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.deleteevent.DeleteEvent;
import com.zoplanner.notification.event.directmessage.DirectMessageEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.event.reminderevent.ReminderEvent;
import com.zoplanner.notification.event.schedulecalendar.ScheduleCalendarDay;
import com.zoplanner.notification.event.schedulecalendar.ScheduleCalendarEvent;
import com.zoplanner.notification.model.Reminder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailTemplateService {

    public String renderNewAssignmentHtml(NewAssignmentEvent e) {

        String html = loadTemplate("email/new-assignment.html");

        return html
                .replace("{{teacherName}}", safe(e.teacherName()))
                .replace("{{assignmentDescription}}", safe(e.assignmentDescription()))
                .replace("{{assignmentDueDate}}",
                        e.assignmentDueDate() !=null ? e.assignmentDueDate().toString() : "-")
                .replace("{{ctaUrl}}", "https://zoplanner.com");
    }

    public String renderNewAssignmentText(NewAssignmentEvent e) {
        return """
                Du har fått en ny uppgift

                Lärare: %s
                Beskrivning: %s
                Deadline: %s
                """.formatted(
                safe(e.teacherName()),
                safe(e.assignmentDescription()),
                e.assignmentDueDate() != null ? e.assignmentDueDate().toString() : "-"
        );
    }



    public String renderScheduleUpdatedHtml(ScheduleUpdateEvent s){

        String html = loadTemplate("email/schedule-updated.html");

        return html
                .replace("{{teacherName}}", safe(s.getTeacherEmail()))
                .replace("{{assignmentDueDate}}", safe(s.getEventTime() !=null ? s.getEventTime().toString() : "-"))
                .replace("{{changes}}", formatChanges(s.getChanges()))
                .replace("{{message}}", s.getMessage())
                .replace("{{ctaUrl}}", "https://zoplanner.com");

    }
    public String renderScheduleUpdatedText(ScheduleUpdateEvent s) {
        return """
        Schedule updated

        Teacher: %s
        Time: %s
        Changes: %s
        Message: %s

        ZoPlanner: https://zoplanner.com
        """.formatted(
                safe(s.getTeacherEmail()),
                safe(s.getEventTime() != null ? s.getEventTime().toString() : "-"),
                safe(s.getChanges() != null ? s.getChanges().toString() : "-"),
                safe(s.getMessage())
        );
    }
    private String formatChanges(List<ScheduleUpdateEvent.ScheduleChange> changes) {

        if (changes == null || changes.isEmpty()) {
            return "-";
        }

        return changes.stream()
                .map(c -> "%s → %s".formatted(
                        safe(c.getOldValue()),
                        safe(c.getNewValue())))
                .collect(java.util.stream.Collectors.joining("<br>"));
    }
    private String loadTemplate(String path) {
        try {
            var resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw new RuntimeException("Template not found in classpath: " + path);
            }
            try (var in = resource.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load template: " + path, e);
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    public String renderDirectMessageHtml(DirectMessageEvent e) {

        String html = loadTemplate("email/direct-message.html");

        return html
                .replace("{{subject}}", safe(e.subject()))
                .replace("{{message}}", safe(e.message()))
                .replace("{{recipientEmail}}", safe(e.recipientEmail()))
                .replace("{{ctaUrl}}", "https://zoplanner.com");
    }

    public String renderDirectMessageText(DirectMessageEvent e) {

        return """
            Nytt meddelande från ZoPlanner

            Ämne: %s

            %s

            ZoPlanner:
            https://zoplanner.com
            """.formatted(
                safe(e.subject()),
                safe(e.message())
        );
    }

    public String renderScheduleCalendarHtml(ScheduleCalendarEvent e) {
        if (isWeeklyView(e)) {
            return renderWeeklyCalendarHtml(e);
        }
        return renderMonthlyCalendarHtml(e);
    }

    private String renderWeeklyCalendarHtml(ScheduleCalendarEvent e) {
        String html = loadTemplate("email/schedule-week.html");

        html = html
                .replace("{{monthTitle}}", safe(e.monthTitle()))
                .replace("{{weekRange}}", safe(e.weekRange()))
                .replace("{{teacherName}}", safe(e.teacherName()))
                .replace("{{ctaUrl}}", "https://zoplanner.com");

        for (int i = 0; i < 7; i++) {
            String placeholder = "{{day" + (i + 1) + "}}";
            String value = "";

            if (e.days() != null && i < e.days().size() && e.days().get(i) != null) {
                value = buildCalendarCell(e.days().get(i));
            }

            html = html.replace(placeholder, value);
        }
        return html;
    }

    private String renderMonthlyCalendarHtml(ScheduleCalendarEvent e) {
        String html = loadTemplate("email/schedule-month.html");

        html = html
                .replace("{{monthTitle}}", safe(e.monthTitle()))
                .replace("{{weekRange}}", safe(e.weekRange()))
                .replace("{{teacherName}}", safe(e.teacherName()))
                .replace("{{ctaUrl}}", "https://zoplanner.com");

        Map<Integer, ScheduleCalendarDay> dayMap = new HashMap<>();

        if (e.days() != null) {
            for (ScheduleCalendarDay day : e.days()) {
                if (day != null && day.dayNumber() != null && !day.dayNumber().isBlank()) {
                    try {
                        int dayNumber = Integer.parseInt(day.dayNumber().trim());
                        dayMap.put(dayNumber, day);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        YearMonth yearMonth = parseYearMonth(e.monthTitle());
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstDay = yearMonth.atDay(1);
        int startOffset = convertToMondayFirstIndex(firstDay.getDayOfWeek());

        for (int cell = 1; cell <= 42; cell++) {
            String placeholder = "{{cell" + cell + "}}";
            String value = "";

            int dayNumber = cell - startOffset;

            if (dayNumber >= 1 && dayNumber <= daysInMonth) {
                ScheduleCalendarDay day = dayMap.get(dayNumber);
                value = buildMonthlyCalendarCell(dayNumber, day);
            }

            html = html.replace(placeholder, value);
        }

        return html;
    }

    private boolean isWeeklyView(ScheduleCalendarEvent e) {
        return e.weekRange() != null && !e.weekRange().isBlank()
                && (e.monthTitle() == null || e.monthTitle().isBlank());
    }

    private YearMonth parseYearMonth(String monthTitle) {
        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("sv", "SE"));

            return YearMonth.parse(
                    monthTitle.toLowerCase(new Locale("sv", "SE")),
                    formatter
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Could not parse monthTitle: " + monthTitle, e
            );
        }
    }

    private int convertToMondayFirstIndex(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 0;
            case TUESDAY -> 1;
            case WEDNESDAY -> 2;
            case THURSDAY -> 3;
            case FRIDAY -> 4;
            case SATURDAY -> 5;
            case SUNDAY -> 6;
        };
    }

    private String buildCalendarCell(ScheduleCalendarDay day) {
        return """
        <div style="font-size:13px; font-weight:700; color:#111827; margin-bottom:6px;">%s</div>
        %s
        """.formatted(
                safe(day.dayNumber()),
                safeHtml(day.contentHtml())
        );
    }

    private String buildMonthlyCalendarCell(int dayNumber, ScheduleCalendarDay day) {
        String content = (day != null) ? safeHtml(day.contentHtml()) : "";

        return """
        <div style="font-size:13px; font-weight:700; color:#111827; margin-bottom:6px;">%d</div>
        %s
        """.formatted(
                dayNumber,
                content
        );
    }

    public String renderScheduleCalendarText(ScheduleCalendarEvent e) {
        String period = isWeeklyView(e) ? e.weekRange() : e.monthTitle();
        return """
                Schema från ZoPlanner
                
                Lärare: %s
                Period: %s
                
                Öppna ZoPlanner för att se hela schemat:
                https://zoplanner.com
                """.formatted(
                        safe(e.teacherName()),
                        safe(period)
        );
    }

    public String renderAssignmentDeletedHtml(DeleteEvent e){

        String html = loadTemplate("email/assignment-deleted.html");


        String subject = "Ett uppdrag har raderats";

        String message = "Uppgiften \"" + safe(e.assignmentDescription()) +
                "\" har raderats från ZoPlanner.";

        return html
                .replace("{{subject}}", subject)
                .replace("{{message}}", message)
                .replace("{{ctaUrl}}", "https://zoplanner.com");
    }

    public String renderAssignmentDeletedText(DeleteEvent e){

        return """
            En uppgift har raderats.

            Lärare: %s
            Uppgift: %s
            Tid: %s

            Öppna ZoPlanner:
            https://zoplanner.com
            """.formatted(
                safe(e.teacherEmail()),
                safe(e.assignmentDescription()),
                safe(e.timestamp() != null ? e.timestamp().toString() : "-")
        );
    }
    public String renderReminderHtml(Reminder r){

        String html = loadTemplate("email/reminder.html");

        return html
                .replace("{{subject}}", "Påminnelse")
                .replace("{{message}}", safe(r.getMessage()))
                .replace("{{ctaUrl}}", "https://zoplanner.com");
    }
    public String renderReminderText(Reminder r){

        return """
            Påminnelse
            
            Meddelande:
            %s
            
            ZoPlanner:
            https://zoplanner.com
            """.formatted(
                safe(r.getMessage())
        );
    }



    private String safeHtml(String s) {
        return (s == null || s.isBlank()) ? "" : s;
    }


}
