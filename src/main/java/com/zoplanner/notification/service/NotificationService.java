package com.zoplanner.notification.service;

import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
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

    public void createNotification(NotificationDTO dto) {
        log.info("Creating notification"); // Övervakning, visar att metod startats
        log.debug("DTO data: {}", dto); // Felsökning, visar vad som skickas in

        try {
            // Konvertera DTO till Entity som ska sparas
            Notification notification = new Notification(dto.getMessage(), dto.getRecipient()); // TODO: lägg till i NotificationDTO
            log.debug("Notification created: {}", notification); // Visar färdig Entity

            notificationRepository.save(notification); // TODO: lägg till i NotificationRepository
            log.info("Notification saved"); // Övervakning

            // new to send a notification when an assignment is created (issue7)
            sendAssignmentCreatedNotification(dto);

        } catch (Exception e) {
            log.error("Error creating notification", e); // Felsökning
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
