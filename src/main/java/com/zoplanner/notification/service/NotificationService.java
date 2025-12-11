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
                    "EMAIL",    // channel (fixed for now)
                    "GENERIC",  // event type (fixed for now)
                    true
            );

        } catch (Exception e) {
            log.error("Error creating notification", e);

            // Try to write audit log even if saving failed
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
}
