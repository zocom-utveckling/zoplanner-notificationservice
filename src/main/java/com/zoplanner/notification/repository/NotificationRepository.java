package com.zoplanner.notification.repository;

import com.zoplanner.notification.model.Notification;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
