package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationTemplate notificationTemplate;
    private final NotificationAuditLogger notificationAuditLogger;
    private final EmailService emailService;
    private NotificationPreference preference;

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
        if ("PER_JOB_24H".equals(dto.getPreference())
                || "WEEKLY_SUMMARY".equals(dto.getPreference())) {
            log.info("Skipping immediate notification due to preference");
            return;
        }

        log.info("Creating notification");
        log.debug("DTO data: {}", dto);

        try {
            // Map DTO to entity (model.Notification has only message + recipient)
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient());
            log.debug("Notification created: {}", notification);

            // Save to repository
            notificationRepository.save(notification);
            log.info("Notification saved");

            // Sends email if channel is EMAIL and we have the email data
            if ("EMAIL".equalsIgnoreCase(dto.getChannel()) && shouldSendEmail(dto)) {
                log.info("Sending email notification to {}", dto.getRecipient());
                sendEmailNotification(dto);
            }

            // Audit log on success
            notificationAuditLogger.logNotificationSent(
                    dto.getRecipient(),
                    "EMAIL",
                    "GENERIC",
                    true
            );

        } catch (Exception e) {
            log.error("Error creating notification", e);

            // audit log on failure (must be NON-FATAL)
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

            // if the failure was ONLY audit logging, do not throw (NON-FATAL)
            if (e.getMessage() != null && e.getMessage().contains("File write failed")) {
                return;
            }

            throw e;
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

    // Method helper to determine if an email should be sent
    private boolean shouldSendEmail(NotificationDTO dto) {
        return dto.getSubject() != null && !dto.getSubject().isEmpty() &&
                dto.getEmailBody() != null && !dto.getEmailBody().isEmpty() &&
                dto.getRecipient() != null && !dto.getRecipient().isEmpty();
    }

    // Method to send email via EmailService
    private void sendEmailNotification(NotificationDTO dto) {
        try {
            String messageId = emailService.sendEmail(
                    dto.getRecipient(),
                    dto.getSubject(),
                    dto.getEmailBody()
            );
            log.info("Email sent successfully with MessageId: {}", messageId);
        } catch (Exception e) {
            log.error("Failed to send the email notification to {}", dto.getRecipient(), e);
        }
    }

}