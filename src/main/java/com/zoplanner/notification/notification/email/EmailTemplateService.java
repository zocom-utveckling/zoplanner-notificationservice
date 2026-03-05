package com.zoplanner.notification.notification.email;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
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
                safe(s.getChanges().toString()),
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

}
