package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.model.Notification;
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
}
