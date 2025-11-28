package com.zoplanner.notification.repository;

import com.zoplanner.notification.model.Notification;

import java.util.List;
import java.util.Optional;

// Tillfälligt för repository, för att kunna köra tester, behöver bytas ut.

public interface NotificationRepository {
    void save(Notification notification);

    Optional<Notification> findById(Long id);               // Hitta notification med ID

    List<Notification> findByUserId(Long userId);           // Hitta alla notifikatioenr för en användare

    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);  // Hitta olästa notifikationer för en användare
}
