package com.zoplanner.notification.notification.email;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.deleteevent.DeleteEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.event.directmessage.DirectMessageEvent;
import com.zoplanner.notification.event.schedulecalendar.ScheduleCalendarEvent;
import com.zoplanner.notification.event.schedulecalendar.ScheduleCalendarDay;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        boolean weeklyView = isWeeklyView(e);

        String html = loadTemplate(
                weeklyView ? "email/schedule-week.html" : "email/schedule-event.html"
        );

        html = html
                .replace("{{monthTitle}}", safe(e.monthTitle()))
                .replace("{{weekRange}}", safe(e.weekRange()))
                .replace("{{teacherName}}", safe(e.teacherName()))
                .replace("{{ctaUrl}}", "https://zoplanner.com");

        int maxDays = weeklyView ? 7 : 31;

        for (int i = 0; i < maxDays; i++) {
            String placeholder = "{{day" + (i + 1) + "}}";
            String value = "";

            if (e.days() != null && i < e.days().size() && e.days().get(i) != null) {
                value = buildCalendarCell(e.days().get(i));
            }
            html = html.replace(placeholder, value);
        }
        return html;
    }

    private boolean isWeeklyView(ScheduleCalendarEvent e) {
        return e.weekRange() != null && !e.weekRange().isBlank()
                && (e.monthTitle() == null || e.monthTitle().isBlank());
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


    private String safeHtml(String s) {
        return (s == null || s.isBlank()) ? "" : s;
    }


}
