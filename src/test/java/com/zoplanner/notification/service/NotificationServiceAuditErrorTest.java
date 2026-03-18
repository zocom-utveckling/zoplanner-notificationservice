package com.zoplanner.notification.service;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.logging.NotificationAuditLogger;
import com.zoplanner.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
public class NotificationServiceAuditErrorTest {

    @Test
    void testAuditLoggerFailureIsHandled(CapturedOutput output) {
        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationTemplate template = mock(NotificationTemplate.class);
        NotificationAuditLogger audit = mock(NotificationAuditLogger.class);
        EmailService emailService = mock(EmailService.class);

        doThrow(new RuntimeException("File write failed"))
                .when(audit)
                .logNotificationSent(anyString(), anyString(), anyString(), anyBoolean());

        NotificationService service = new NotificationService(repo, template, audit, emailService);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        assertThatThrownBy(() -> service.createNotification(dto))
                .isInstanceOf(RuntimeException.class)
                        .hasMessage("File write failed");

        assertThat(output).contains("Error creating notification");
        assertThat(output).contains("File write failed");

        verify(repo, times(1)).save(any());
        verify(audit, times(2))
                .logNotificationSent(anyString(), anyString(), anyString(), anyBoolean());
    }
}
