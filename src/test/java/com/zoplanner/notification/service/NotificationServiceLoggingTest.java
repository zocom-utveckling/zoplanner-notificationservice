package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)

public class NotificationServiceLoggingTest {

    @Test
    void testLogging(CapturedOutput output) {

        //Arrange
        NotificationRepository repo = Mockito.mock(NotificationRepository.class);
        NotificationAuditLogger auditLogger = Mockito.mock(NotificationAuditLogger.class);
        NotificationService service = new NotificationService(repo, auditLogger);
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        //Act
        service.createNotification(dto);

        //Assert
        assertThat(output).contains("Creating notification"); //Info loggning
        assertThat(output).contains("Notification saved"); //Debug loggning
        assertThat(output).doesNotContain("Error"); //Inga fel hittats
    }
}
