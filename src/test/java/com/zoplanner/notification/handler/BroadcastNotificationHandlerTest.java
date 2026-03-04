package com.zoplanner.notification.handler;

import com.zoplanner.notification.dto.EmailType;
import com.zoplanner.notification.event.broadcast.BroadcastNotificationEvent;
import com.zoplanner.notification.notification.email.EmailTemplateService;
import com.zoplanner.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BroadcastNotificationHandlerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService templateService;

    @InjectMocks
    private BroadcastNotificationHandler handler;

    @Test
    void shouldSendHtmlEmailToAllRecipients() {

        // arrange
        BroadcastNotificationEvent event = new BroadcastNotificationEvent(
                "BROADCAST_NOTIFICATION",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                "m1",
                "AllMyConsultants",
                List.of("a@test.com", "b@test.com"),
                "Info",
                "Hello consultants",
                EmailType.HTML
        );

        when(templateService.renderBroadcastHtml(anyString(), anyString(), anyString()))
                .thenReturn("<html>body</html>");

        // act
        handler.handle(event);

        // assert
        verify(emailService, times(2)).sendHtmlEmail(anyString(), eq("Info"), eq("<html>body</html>"));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldSendTextEmailWhenEmailTypeIsText() {

        // arrange
        BroadcastNotificationEvent event = new BroadcastNotificationEvent(
                "BROADCAST_NOTIFICATION",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                "m1",
                "AllMyConsultants",
                List.of("a@test.com"),
                "Info",
                "Hello consultants",
                EmailType.TEXT
        );

        when(templateService.renderBroadcastText(anyString(), anyString(), anyString()))
                .thenReturn("text body");

        // act
        handler.handle(event);

        // assert
        verify(emailService, times(1)).sendEmail(eq("a@test.com"), eq("Info"), eq("text body"));
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }
}