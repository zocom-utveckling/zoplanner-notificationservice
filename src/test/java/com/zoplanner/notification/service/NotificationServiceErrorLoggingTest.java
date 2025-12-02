package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
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
        NotificationRepository repo = mock(NotificationRepository.class);
        EmailService emailService = mock(EmailService.class);
        NotificationService service = new NotificationService(repo, emailService);

        //Gör så att repository skapar exception
        doThrow(new RuntimeException("Database failure"))
                .when(repo)
                .save(any());

        //Fake DTO
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        //Act anropa service och förvänta exception
        try {
            service.createNotification(dto);
        } catch (RuntimeException e) {
            // Expected exception
        }

        //Assert loggning
        assertThat(output).contains("Error");
        assertThat(output).contains("Error creating notification");

        //Ska inte ha med log över lyckade anrop
        assertThat(output).doesNotContain("Notification saved");
    }
}
