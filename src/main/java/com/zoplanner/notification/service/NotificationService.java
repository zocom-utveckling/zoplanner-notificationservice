package com.zoplanner.notification.service;

import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.dto.EmailType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplate notificationTemplate; // new (issue7)


    // Dependency injection, Spring ger Repository automatiskt
    public NotificationService(NotificationRepository notificationRepository,
                               NotificationTemplate notificationTemplate) { // new (issue7)
        this.notificationRepository = notificationRepository;
        this.notificationTemplate = notificationTemplate; // new (issue7)
    }

    /**
     * Skapar en notifikation och skickar e-post
     *
     * @param dto NotificationDTO med all information som behövs
     */
    public void createNotification(NotificationDTO dto) {
        log.info("Creating notification for recipient: {}", dto.getRecipient());
        log.debug("DTO data: {}", dto);

        try {
            // 1. Spara till databas
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient());
            log.debug("Notification created: {}", notification);
            notificationRepository.save(notification);
            log.info("Notification saved to database");

            // 2. Skicka email om email-fält finns
            if (shouldSendEmail(dto)) {
                sendEmailNotification(dto);
            } else {
                log.debug("No email fields provided, skipping email sending");
            }

            // 3. Logga framgång till audit-fil
            notificationAuditLogger.logNotificationSent(
                    dto.getRecipient(),
                    dto.getChannel() != null ? dto.getChannel() : "EMAIL",
                    dto.getEventType() != null ? dto.getEventType() : "GENERIC",
                    true
            );

        } catch (Exception e) {
            log.error("Error creating notification for recipient: {}", dto.getRecipient(), e);

            // Logga misslyckande till audit-fil
            try {
                notificationAuditLogger.logNotificationSent(
                        dto.getRecipient(),
                        dto.getChannel() != null ? dto.getChannel() : "EMAIL",
                        dto.getEventType() != null ? dto.getEventType() : "GENERIC",
                        false
                );
            } catch (Exception auditException) {
                log.error("Failed to write audit log after failure", auditException);
            }
        }
    }

    private boolean shouldSendEmail(NotificationDTO dto) {
        return dto.getSubject() != null && !dto.getSubject().isEmpty() &&
               dto.getEmailBody() != null && !dto.getEmailBody().isEmpty() &&
               dto.getRecipient() != null && !dto.getRecipient().isEmpty();
    }

    private void sendEmailNotification(NotificationDTO dto) {
        try {
            String messageId;

            if (dto.getEmailType() == EmailType.HTML) {
                log.info("Sending HTML email to: {}", dto.getRecipient());
                messageId = emailService.sendHtmlEmail(
                    dto.getRecipient(),
                    dto.getSubject(),
                    dto.getEmailBody()
                );
            } else {
                log.info("Sending text email to: {}", dto.getRecipient());
                messageId = emailService.sendEmail(
                    dto.getRecipient(),
                    dto.getSubject(),
                    dto.getEmailBody()
                );
            }

            log.info("Email sent successfully with MessageId: {}", messageId);

            // new to send a notification when an assignment is created (issue7)
            sendAssignmentCreatedNotification(dto);

        } catch (Exception e) {
            log.error("Failed to send email to: {}. Notification saved but email not sent.",
                     dto.getRecipient(), e);
            // Note: Audit logging happens in createNotification, not here
        }
    }

    // new helper method that builds and sends a message for assignment created (issue7)
    private void sendAssignmentCreatedNotification(NotificationDTO dto) {

        // build the message text using the NotificationTemplate class
        String text = notificationTemplate.buildAssignmentCreatedMessage(
                dto.getRecipientName(),
                dto.getAssignmentTitle()
        );

        // in a real system we would call an email or sms service here.
        // but for now we just log that we are sending the message.
        log.info("Sending 'assignment created' notification to {}", dto.getRecipient());
        log.debug("Notification message body:\n{}", text);
    }
}
