package com.zoplanner.notification.repository;

import com.zoplanner.notification.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findBySendAtBeforeAndStatus(LocalDateTime time, String status);

}
