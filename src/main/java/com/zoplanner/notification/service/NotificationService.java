package com.zoplanner.notification.service;

import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.dto.EmailType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
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
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient());
            log.debug("Notification created: {}", notification);

            notificationRepository.save(notification);
            log.info("Notification saved to database");

            if (shouldSendEmail(dto)) {
                sendEmailNotification(dto);
            } else {
                log.debug("No email fields provided, skipping email sending");
            }

        } catch (Exception e) {
            log.error("Error creating notification for recipient: {}", dto.getRecipient(), e);
            throw new RuntimeException("Failed to create notification", e);
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

        } catch (Exception e) {
            log.error("Failed to send email to: {}. Notification saved but email not sent.",
                     dto.getRecipient(), e);
        }
    }
}
