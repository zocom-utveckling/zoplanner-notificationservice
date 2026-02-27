package com.zoplanner.notification.notification.email;

import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;

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
