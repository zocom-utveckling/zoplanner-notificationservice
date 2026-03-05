package com.zoplanner.notification.notification;

import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.notification.email.EmailTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "notification.publisher", havingValue = "ses") // Används endast om properties är inställda för SES
public class SesNotificationPublisher implements NotificationPublisher {

    private final SesClient sesClient;
    private final String fromEmail;
    private final String fromName;
    private final EmailTemplateService templateService;

    @SuppressWarnings("unused")
    private final MessageSource messageSource;

    public SesNotificationPublisher(
            SesClient sesClient,
            @Value("${aws.ses.from.email}") String fromEmail,
            @Value("${aws.ses.from.name:}") String fromName,
            MessageSource messageSource,
            EmailTemplateService templateService
    ) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.messageSource = messageSource;
        this.templateService = templateService;

        // Log för att visa att SES startat
        log.info("SesNotificationPublisher initialized (fromEmail={}, fromName={})",
                fromEmail, fromName);
    }

    // Skickar ett mail för NEW_ASSIGNMENT
    @Override
    public void publishNewAssignment(NewAssignmentEvent event) {
        requireFromEmail();

        String recipient = safe(event.teacherEmail()); // Hämta mottagar mail, kontroll att det inte är null, om det saknas, logga och avbryt.
        if (recipient.equals("-")) {
            log.warn("Missing teacherEmail on NEW_ASSIGNMENT event. Not publishing to SES. event={}", event);
            return;
        }

        String subject = "NEW_ASSIGNMENT";
        String htmlBody = templateService.renderNewAssignmentHtml(event);
        String textBody = templateService.renderNewAssignmentText(event);

        SendEmailResponse response = sendEmail(recipient, subject, htmlBody, textBody);
        log.info("Sent NEW_ASSIGNMENT email via SES, messageId={} to={} from={}", response.messageId(), recipient, fromEmail);
    }


    // Skickar ett mail för SCHEDULE_UPDATED
    @Override
    public void publishScheduleUpdate(ScheduleUpdateEvent event) {
        requireFromEmail();

        String recipient = safe(event.getTeacherEmail());
        if (recipient.equals("-")) {
            log.warn("Missing teacherEmail on SCHEDULE_UPDATED event. Not publishing to SES. event={}", event);
            return;
        }

        String subject = "SCHEDULE_UPDATED";

        String htmlBody = templateService.renderScheduleUpdatedHtml(event);
        String textBody = templateService.renderScheduleUpdatedText(event);

        SendEmailResponse response = sendEmail(recipient, subject, htmlBody, textBody);

        log.info(
                "Sent SCHEDULE_UPDATED email via SES, messageId={} to={} from={}",
                response.messageId(),
                recipient,
                fromEmail
        );
    }

    // Metod för att ta emot text-email från  AWS SES
    private SendEmailResponse sendEmail (
            String recipient,
            String subject,
            String htmlBody,
            String textBody
    ) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(formatFrom(fromName, fromEmail))
                .destination(Destination.builder()
                        .toAddresses(List.of(recipient))
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .data(subject)
                                .charset("UTF-8")
                                .build())
                        .body(Body.builder()
                                .html(Content.builder()
                                        .data(htmlBody)
                                        .charset("UTF-8")
                                        .build())
                                .text(Content.builder()
                                        .data(textBody)
                                        .charset("UTF-8")
                                        .build())
                                .build())
                        .build())
                .build();
        return sesClient.sendEmail(request);
    }

    // För att säkerställa att från mail finns.
    private void requireFromEmail() {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("SES from email is empty. Check aws.ses.from.email or AWS_SES_FROM_EMAIL.");
        }
    }

    // Formaterar avsändare som Name
    private String formatFrom(String name, String email) {
        if (name == null || name.isBlank()) return email;
        return String.format("%s <%s>", name, email);
    }

    // Bygger texten i mailet för NEW_ASSIGNMENT (Ska ändras till HTML template)
    private String buildEmailMessage(NewAssignmentEvent e) {
        return """
                Du har fått en ny uppgift:

                Lärare: %s
                Beskrivning: %s
                Deadline: %s
                """.formatted(
                safe(e.teacherName()),
                safe(e.assignmentDescription()),
                e.assignmentDueDate() != null ? e.assignmentDueDate().toString() : "-"
        );
    }


    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
