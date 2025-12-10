package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.EmailType;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplate notificationTemplate;
    private final EmailService emailService;
    private final NotificationAuditLogger notificationAuditLogger;// new (issue7)


    // Dependency injection, Spring ger Repository automatiskt
    public NotificationService(NotificationRepository notificationRepository,
                               NotificationTemplate notificationTemplate,
                               NotificationAuditLogger notificationAuditLogger,
                               EmailService emailService) { // new (issue7)

        this.notificationRepository = notificationRepository;
        this.notificationTemplate = notificationTemplate; // new (issue7)
        this.emailService = emailService;
        this.notificationAuditLogger = notificationAuditLogger;
    }

    public void createNotification(NotificationDTO dto) {
        log.info("Creating notification");
        log.debug("DTO data: {}", dto);

        try {
            // 1. Spara notifikationen (in-memory / databas beroende på implementation av repository)
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient());
            log.debug("Notification created: {}", notification);
            notificationRepository.save(notification);
            log.info("Notification saved");

            // 2. Skicka e-post om kanalen är EMAIL och vi har ett mail-innehåll
            if ("EMAIL".equalsIgnoreCase(dto.getChannel()) && dto.getEmailBody() != null) {
                try {
                    String recipient = dto.getRecipient();
                    String subject = dto.getSubject() != null ? dto.getSubject() : "Notification";
                    String emailBody = dto.getEmailBody();

                    emailService.sendEmail(recipient, subject, emailBody);
                    log.info("Email sent for notification to {}", dto.getRecipient());
                } catch (Exception emailException) {
                    log.error("Failed to send email notification to {}", dto.getRecipient(), emailException);
                    // vi låter ändå huvudflödet fortsätta – utskicket är redan sparat och auditloggas nedan
                }
            }

            // 3. Audit-logg – med defaultvärden om kanal/eventType saknas
            notificationAuditLogger.logNotificationSent(
                    dto.getRecipient(),
                    dto.getChannel() != null ? dto.getChannel() : "EMAIL",
                    dto.getEventType() != null ? dto.getEventType() : "GENERIC",
                    true
            );

        } catch (Exception e) {
            // Detta loggmeddelande är det som testerna letar efter
            log.error("Error creating notification", e);

            // Försök audit-logga misslyckandet
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

    // Markera notifikation som läst
    public NotificationDTO markAsRead(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);  // Hitta notifikationen i databasen

        if (optionalNotification.isEmpty()) {
            throw new RuntimeException("Notification with id " + notificationId + " not found");  // "Kasta" ett fel om den inte finns
        }

        Notification notification = optionalNotification.get(); // Hämtar notifikation och ändrar isRead till true
        notification.setRead(true);

        notificationRepository.save(notification);

        return convertToDTO(notification);
    }

    // Hämta alla notifikationer för en användare
    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        // Konvertera alla notifikation entities till DTOs
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // HÄmta endast olästa notifikationer för en användare
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, false);

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Konverterar Entity till DTO
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setRecipient(notification.getRecipient());
        notificationDTO.setUserId(notification.getUserId());
        notificationDTO.setRead(notification.isRead());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        return notificationDTO;
    }
}






