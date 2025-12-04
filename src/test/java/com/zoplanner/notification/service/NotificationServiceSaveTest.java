package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NotificationServiceSaveTest {

    @Test
    void testNotificationObjectPassedToRepository() {

        //Arrange
        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationAuditLogger auditLogger = mock(NotificationAuditLogger.class);
        EmailService emailService = mock(EmailService.class);
        NotificationService service = new NotificationService(repo, auditLogger, emailService);

        //DTO med fejkdata
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Test message");
        dto.setRecipient("test@test.com");

        //ArgumentCaptor fångar vad som skickas in i repo.save()
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        //Act
        service.createNotification(dto);

        //Assert
        verify(repo, times(1)).save(captor.capture());

        //Hämta objektet som skickades in i repo.save()
        Notification savedNotification = captor.getValue();

        //Kontrollera att värden stämmer
        assertThat(savedNotification.getMessage()).isEqualTo("Test message");
        assertThat(savedNotification.getRecipient()).isEqualTo("test@test.com");

    }
}
