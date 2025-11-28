package com.zoplanner.notification.service;

import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.model.EmailLog;
import com.zoplanner.notification.repository.EmailLogRepository;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service

public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailLogRepository emailLogRepository;

    // Dependency injection, Spring ger Repository automatiskt
    public NotificationService(NotificationRepository notificationRepository,
                               EmailLogRepository emailLogRepository) {
        this.notificationRepository = notificationRepository;
        this.emailLogRepository = emailLogRepository;
    }

    public void createNotification(NotificationDTO dto) {

        if (dto == null) {
            log.error("Recieved null NotificationDTO");
            return;
        }

        log.info("Creating notification"); // Övervakning, visar att metod startats
        log.debug("DTO data: {}", dto); // Felsökning, visar vad som skickas in

        try {
            // Konvertera DTO till Entity som ska sparas
            Notification notification = new Notification(
                    dto.getMessage(),
                    dto.getRecipient());

            notificationRepository.save(notification);
            log.info("Notification saved");

            // Email log för lyckade anrop
            EmailLog logEntry = new EmailLog();
            logEntry.setRecipient(dto.getRecipient());
            logEntry.setSubject("Notification");
            logEntry.setMessage(dto.getMessage());
            logEntry.setSentAt(LocalDateTime.now());
            logEntry.setSuccess(true);
            logEntry.setErrorMessage(null);

            emailLogRepository.save(logEntry);
            log.debug("Troubleshooting details: Email log for successful notification");

        } catch (Exception e) {
            log.error("Error creating notification", e); // Felsökning

            // Email log för misslyckade anrop
            EmailLog logEntry = new EmailLog();
            logEntry.setRecipient(dto.getRecipient());
            logEntry.setSubject("notification");
            logEntry.setMessage(dto.getMessage());
            logEntry.setSentAt(LocalDateTime.now());
            logEntry.setSuccess(false);
            logEntry.setErrorMessage(e.getMessage());

            emailLogRepository.save(logEntry);
            log.debug("Troubleshooting details: Email log for failed notification");
        }
    }
}
