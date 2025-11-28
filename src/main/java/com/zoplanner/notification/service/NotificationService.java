package com.zoplanner.notification.service;

import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service

public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Dependency injection, Spring ger Repository automatiskt
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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

        } catch (Exception e) {
            log.error("Error creating notification", e); // Felsökning
        }
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
}
