package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.zoplanner.notification.dto.BroadcastNotificationDTO;

import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTemplate notificationTemplate;
    private final NotificationAuditLogger notificationAuditLogger;
    private final EmailService emailService;

    // Constructor used by Spring + tests
    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationTemplate notificationTemplate,
            NotificationAuditLogger notificationAuditLogger,
            EmailService emailService
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationTemplate = notificationTemplate;
        this.notificationAuditLogger = notificationAuditLogger;
        this.emailService = emailService;
    }

    public void createNotification(NotificationDTO dto) {
        log.info("Creating notification");
        log.debug("DTO data: {}", dto);

        try {
            // Map DTO to entity (model.Notification has only message + recipient)
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient());
            log.debug("Notification created: {}", notification);

            // Save to repository
            notificationRepository.save(notification);
            log.info("Notification saved");

            // Audit log on success
            notificationAuditLogger.logNotificationSent(
                    dto.getRecipient(),
                    "EMAIL",
                    "GENERIC",
                    true
            );

        } catch (Exception e) {
            log.error("Error creating notification", e);

            try {
                notificationAuditLogger.logNotificationSent(
                        dto.getRecipient(),
                        "EMAIL",
                        "GENERIC",
                        false
                );
            } catch (Exception auditException) {
                log.error("Failed to write audit log after failure", auditException);
            }
        }
    }

    // new issue8
    public void sendAssignmentUpdatedNotification(NotificationDTO dto) {
        log.info("Sending notification for updated assignment");
        log.debug("DTO data: {}", dto);

        try {
            String assignmentTitle = dto.getSubject();
            if (assignmentTitle == null || assignmentTitle.isBlank()) {
                assignmentTitle = "unknown assignment";
            }

            // build text using template. new issue8
            String text = notificationTemplate.buildAssignmentUpdatedMessage(
                    dto.getRecipient(),
                    assignmentTitle
            );

            String channel = dto.getChannel();

            if ("SMS".equalsIgnoreCase(channel)) {
                // sms not implemented yet, only log. new issue8
                log.info("Sending sms (simulated) to {}", dto.getRecipient());
                log.debug("sms body:\n{}", text);
                return;
            }

            // default is email. new issue8
            log.info("Sending email for assignment updated to {}", dto.getRecipient());
            emailService.sendEmail(dto.getRecipient(), "Assignment updated", text);

        } catch (Exception e) {
            log.error("Error sending assignment updated notification", e);
            throw e;
        }
    }

    // new issue9
    public void sendAssignmentDeletedNotification(NotificationDTO dto) {
        log.info("Sending notification for deleted assignment");
        log.debug("DTO data: {}", dto);

        try {
            String assignmentTitle = dto.getSubject();
            if (assignmentTitle == null || assignmentTitle.isBlank()) {
                assignmentTitle = "unknown assignment";
            }

            // build text using template. new issue9
            String text = notificationTemplate.buildAssignmentDeletedMessage(
                    dto.getRecipient(),
                    assignmentTitle
            );

            String channel = dto.getChannel();

            if ("SMS".equalsIgnoreCase(channel)) {
                // sms not implemented yet, only log. new issue9
                log.info("Sending sms (simulated) to {}", dto.getRecipient());
                log.debug("sms body:\n{}", text);
                return;
            }

            // default is email. ew issue9
            log.info("Sending email for assignment deleted to {}", dto.getRecipient());
            emailService.sendEmail(dto.getRecipient(), "Assignment deleted", text);

        } catch (Exception e) {
            log.error("Error sending assignment deleted notification", e);
            throw e;
        }
    }

    // new issue20
    // retry helper, tries to run the action again if it fails.
    private void runWithRetry(Runnable action) {

        int maxAttempts = 3; // how many times we try.
        long delayMs = 500;  // wait time between tries (ms).

        // loop for each try.
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try { // try to run the action.
                action.run();
                return; // success, stop retrying.

            } catch (Exception e) { // if it fails we retry.
                log.warn("send failed attempt {}/{}", attempt, maxAttempts);

                // if this was the last try, we throw the error.
                if (attempt == maxAttempts) {
                    throw e;
                }

                // wait before next attempt.
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) { // if someone stops the thread.
                    Thread.currentThread().interrupt(); // keep interrupt status.
                    throw new RuntimeException("retry interrupted", ie);
                }
            }
        }
    }

    // new issue111
    public void broadcastNotification(BroadcastNotificationDTO dto) {
        log.info("Sending broadcast notification");
        log.debug("DTO data: {}", dto);

        try {
            String subject = dto.getSubject();
            if (subject == null || subject.isBlank()) {
                subject = "Broadcast";
            }

            String channel = dto.getChannel();
            if (channel == null || channel.isBlank()) {
                channel = "EMAIL";
            }

            // loop all recipients and send the same message
            for (String email : dto.getRecipientEmails()) {

                // save to repository
                Notification notification = new Notification(dto.getMessage(), email);
                notificationRepository.save(notification);

                if ("SMS".equalsIgnoreCase(channel)) {
                    log.info("Sending sms (simulated) to {}", email);
                    log.debug("sms body:\n{}", dto.getMessage());

                    // audit log on success for each recipient
                    notificationAuditLogger.logNotificationSent(
                            email,
                            channel,
                            "BROADCAST",
                            true
                    );
                    continue;
                }

                // send email via AWS path (SES) using HTML template
                log.info("Sending broadcast email to {}", email);

                String htmlBody = loadBroadcastHtml(dto.getMessage());

                // IMPORTANT: use the HTML/AWS sending method in EmailService (SES)
                emailService.sendHtmlEmail(email, subject, htmlBody);

                // audit log on success for each recipient
                notificationAuditLogger.logNotificationSent(
                        email,
                        channel,
                        "BROADCAST",
                        true
                );
            }

        } catch (Exception e) {
            log.error("Error sending broadcast notification", e);
            try {
                if (dto.getRecipientEmails() != null) {
                    String channel = dto.getChannel();
                    if (channel == null || channel.isBlank()) {
                        channel = "EMAIL";
                    }

                    for (String email : dto.getRecipientEmails()) {
                        notificationAuditLogger.logNotificationSent(
                                email,
                                channel,
                                "BROADCAST",
                                false
                        );
                    }
                }
            } catch (Exception auditException) {
                log.error("Failed to write audit log after failure", auditException);
            }

            throw e;
        }
    }

    private String loadBroadcastHtml(String message) {
        try {
            ClassPathResource resource = new ClassPathResource("email/broadcast.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String safeMessage = (message == null) ? "" : message
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");

            // Replace the Thymeleaf placeholder with real content.
            // (Simple and stable for sending out final HTML)
            template = template.replace("<p th:text=\"${message}\">MESSAGE</p>", "<p>" + safeMessage + "</p>");
            template = template.replace("th:text=\"${message}\"", "");

            return template;

        } catch (IOException e) {
            log.warn("Could not load broadcast.html template, falling back to simple html. Reason: {}", e.getMessage());
            String safeMessage = (message == null) ? "" : message
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
            return "<html><body><p>" + safeMessage + "</p></body></html>";
        }
    }
}