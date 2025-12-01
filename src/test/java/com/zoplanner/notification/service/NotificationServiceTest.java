package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.model.EmailLog;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.EmailLogRepository;
import com.zoplanner.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Test
    void testCreateNotification() {

        // Arrange
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        EmailLogRepository emailLogRepository = Mockito.mock(EmailLogRepository.class);
        NotificationService service = new NotificationService(notificationRepository,emailLogRepository);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        // Act
        service.createNotification(dto);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotificationIsSavedToRepository() {

        // Arrange
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        EmailLogRepository emailLogRepository = mock(EmailLogRepository.class);
        NotificationService service = new NotificationService(notificationRepository,emailLogRepository);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Test message");
        dto.setRecipient("test@test.com");

        // Act
        service.createNotification(dto);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getMessage()).isEqualTo("Test message");
        assertThat(saved.getRecipient()).isEqualTo("test@test.com");

        // Assert Emaillog saved
        ArgumentCaptor<EmailLog> emailLogCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(emailLogCaptor.capture());

        EmailLog log = emailLogCaptor.getValue();

        assertThat(log.getRecipient()).isEqualTo("test@test.com");
        assertThat(log.getMessage()).isEqualTo("Test message");
        assertThat(log.isSuccess()).isTrue();
        assertThat(log.getErrorMessage()).isNull();

    }
}
