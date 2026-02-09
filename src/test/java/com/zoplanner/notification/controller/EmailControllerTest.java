package com.zoplanner.notification.controller;


import com.zoplanner.notification.dto.EmailRequest;
import com.zoplanner.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class EmailControllerTest {


    @Test
    void testHealthEndpoint() {
        // Arrange
        EmailService emailService = mock(EmailService.class);
        EmailController controller = new EmailController(emailService);

        // Act
        ResponseEntity<Map<String, String>> response = controller.health();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("ok", body.get("status"));
        assertEquals("EmailController", body.get("service"));
    }

    @Test
    void testSendEmailSuccess() {
        // Arrange
        EmailService emailService = mock(EmailService.class);
        EmailController controller = new EmailController(emailService);

        EmailRequest request = new EmailRequest("test@example.com", "Test", "Body", false);
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn("mock-message-id-123");

        // Act
        ResponseEntity<Map<String, String>> response = controller.sendEmail(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.get("Status"));
        assertEquals("mock-message-id-123", body.get("MessageId"));
        verify(emailService, times(1)).sendEmail("test@example.com", "Test", "Body");
    }
}
