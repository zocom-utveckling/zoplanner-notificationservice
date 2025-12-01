package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.repository.EmailLogRepository;
import com.zoplanner.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
public class NotificationServiceErrorLoggingTest {

    @Test
    void testErrorLoggingWhenRepositoryFails(CapturedOutput output) {

        //Arrange, mock repository och service
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        EmailLogRepository emailLogRepository = mock(EmailLogRepository.class);
        NotificationService service = new NotificationService(notificationRepository,emailLogRepository);

        //Gör så att repository skapar exception
        doThrow(new RuntimeException("Database failure"))
                .when(notificationRepository)
                .save(any());

        //Fake DTO
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        //Act anropa service
        service.createNotification(dto);

        //Assert loggning
        assertThat(output).contains("Error");
        assertThat(output).contains("Database failure");
        assertThat(output).contains("Error creating notification");

        //Ska inte ha med log över lyckade anrop
        assertThat(output).doesNotContain("Notification saved");
    }
}
