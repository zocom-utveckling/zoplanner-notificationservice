package com.zoplanner.notification.repository;

import com.zoplanner.notification.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enkel in-memory repository.
 * Ingen Spring Data JPA, bara en lista i minnet.
 */
@Repository
public class NotificationRepository {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(idGenerator.getAndIncrement());
        }
        notifications.add(notification);
        return notification;
    }

    public List<Notification> findAll() {
        return List.copyOf(notifications);
    }

    public Optional<Notification> findById(Long id) {
        return notifications.stream()
                .filter(notification -> notification.getId() != null && notification.getId().equals(id))
                .findFirst();
    }

    public List<Notification> findByUserId(Long userId) {
        return notifications.stream()
                .filter(notification -> notification.getUserId() != null && notification.getUserId().equals(userId))
                .toList();
    }

    public List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead) {
        return notifications.stream()
                .filter(notification -> notification.getUserId() != null && notification.getUserId().equals(userId))
                .filter(notification -> notification.isRead() == isRead)
                .toList();
    }
}
