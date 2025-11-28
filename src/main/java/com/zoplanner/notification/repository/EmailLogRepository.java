package com.zoplanner.notification.repository;

import org.springframework.stereotype.Repository;
import com.zoplanner.notification.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository för att spara emailloggar

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
}
