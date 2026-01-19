package com.zoplanner.notification.handler;

import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.service.EmailService;
import com.zoplanner.notification.service.NotificationTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewAssignmentNotificationHandlerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationTemplate notificationTemplate;

    @InjectMocks
    private NewAssignmentNotificationHandler handler;

    @Test
    void shouldSendEmailForNewAssignmentEvent() {

        NewAssignmentEvent event = new NewAssignmentEvent(
                "NEW_ASSIGNMENT",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                "t1",
                "test testsson",
                "test@test.com",
                "a1",
                "test assignment",
                LocalDate.now().plusDays(7)
        );

        when(notificationTemplate.buildAssignmentCreatedMessage(
                any(),
                any()
        )).thenReturn("email body");

        handler.handle(event);

        verify(emailService).sendEmail(
                eq("test@test.com"),
                anyString(),
                eq("email body")
        );
    }
}
