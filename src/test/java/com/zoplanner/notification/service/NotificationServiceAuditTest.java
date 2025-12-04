package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.model.Notification;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NotificationServiceAuditTest {

    @Test
    void testNotificationIsSavedToRepository() {

        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationAuditLogger audit = mock(NotificationAuditLogger.class);
        NotificationService service = new NotificationService(repo, audit);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        service.createNotification(dto);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getMessage()).isEqualTo("Hello");
        assertThat(saved.getRecipient()).isEqualTo("test@test.com");


    }

    @Test
    void testAuditLoggerIsCalled() {

        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationAuditLogger audit = mock(NotificationAuditLogger.class);
        NotificationService service = new NotificationService(repo, audit);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        service.createNotification(dto);

        verify(audit, times(1)).logNotificationSent("test@test.com", "EMAIL", "GENERIC", true);

    }
}
