package com.zoplanner.notification.repository;

import com.zoplanner.notification.model.Notification;

// Tillfälligt för repository, för att kunna köra tester, behöver bytas ut.

public interface NotificationRepository {
    void save(Notification notification);
}
