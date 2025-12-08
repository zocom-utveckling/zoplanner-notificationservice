package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;

import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Test
    void testCreateNotification() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        NotificationTemplate template = Mockito.mock(NotificationTemplate.class);
        NotificationAuditLogger auditLogger = Mockito.mock(NotificationAuditLogger.class);
        EmailService emailService = Mockito.mock(EmailService.class);
        NotificationService service = new NotificationService(repository, template, auditLogger, emailService);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        service.createNotification(dto);
        verify(repository, times(1)).save(any(Notification.class));
    }

}
