package com.zoplanner.notification.handler;

import com.zoplanner.notification.dto.EmailType;
import com.zoplanner.notification.event.broadcast.BroadcastNotificationEvent;
import com.zoplanner.notification.notification.email.EmailTemplateService;
import com.zoplanner.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class BroadcastNotificationHandler {

    private final EmailService emailService;
    private final EmailTemplateService templateService;

    public BroadcastNotificationHandler(EmailService emailService, EmailTemplateService templateService) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    public void handle(BroadcastNotificationEvent event) {
        if (event == null) {
            log.warn("Broadcast event is null, skipping.");
            return;
        }

        if (event.recipientEmails() == null || event.recipientEmails().isEmpty()) {
            log.warn("Broadcast event has no recipients. eventId={}", event.eventId());
            return;
        }

        String subject = StringUtils.hasText(event.subject())
                ? event.subject()
                : "Meddelande från konsultchef";

        EmailType emailType = (event.emailType() != null) ? event.emailType() : EmailType.HTML;

        for (String recipient : event.recipientEmails()) {
            if (!StringUtils.hasText(recipient)) {
                continue;
            }

            try {
                if (emailType == EmailType.TEXT) {
                    String textBody = templateService.renderBroadcastText(
                            event.managerId(),
                            event.recipientGroup(),
                            event.message()
                    );
                    emailService.sendEmail(recipient, subject, textBody);
                } else {
                    String htmlBody = templateService.renderBroadcastHtml(
                            event.managerId(),
                            event.recipientGroup(),
                            event.message()
                    );
                    emailService.sendHtmlEmail(recipient, subject, htmlBody);
                }

                log.info("Broadcast email sent to {} (eventId={})", recipient, event.eventId());

            } catch (Exception ex) {
                // If one recipient fails, we still try the rest
                log.error("Failed sending broadcast to {} (eventId={})", recipient, event.eventId(), ex);
            }
        }
    }
}