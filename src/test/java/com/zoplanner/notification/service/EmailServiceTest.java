package com.zoplanner.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för EmailService
 * Testar att AWS SES anropas korrekt vid utskick av e-post
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    private EmailService emailService;

    private final String testFromEmail = "noreply@zoplanner.com";
    private final String testFromName = "ZoPlanner Test";
    private final String testConfigSet = "test-config-set";

    @BeforeEach
    void setUp() {
        emailService = new EmailService(sesClient);

        // Sätt properties via reflection (eftersom @Value inte fungerar i unit tests)
        ReflectionTestUtils.setField(emailService, "fromEmail", testFromEmail);
        ReflectionTestUtils.setField(emailService, "fromName", testFromName);
        ReflectionTestUtils.setField(emailService, "configurationSet", testConfigSet);
    }

    @Test
    void sendEmail_ShouldCallSesClientWithCorrectParameters() {
        // Arrange
        String toEmail = "teacher@example.com";
        String subject = "New Assignment";
        String body = "You have been assigned to a new class.";
        String expectedMessageId = "test-message-id-12345";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId(expectedMessageId)
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        String result = emailService.sendEmail(toEmail, subject, body);

        // Assert
        assertEquals(expectedMessageId, result);

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals(toEmail, capturedRequest.destination().toAddresses().get(0));
        assertEquals(subject, capturedRequest.message().subject().data());
        assertEquals(body, capturedRequest.message().body().text().data());
        assertTrue(capturedRequest.source().contains(testFromEmail));
        assertEquals(testConfigSet, capturedRequest.configurationSetName());
    }

    @Test
    void sendEmail_WithoutConfigurationSet_ShouldNotIncludeConfigSet() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "configurationSet", "");

        String toEmail = "teacher@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("msg-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(toEmail, subject, body);

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertNull(capturedRequest.configurationSetName());
    }

    @Test
    void sendEmail_WhenSesThrowsException_ShouldPropagateException() {
        // Arrange
        String toEmail = "invalid@example.com";
        String subject = "Test";
        String body = "Test";

        SesException sesException = (SesException) SesException.builder()
                .message("Email address not verified")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(sesException);

        // Act & Assert
        assertThrows(SesException.class, () -> {
            emailService.sendEmail(toEmail, subject, body);
        });

        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendHtmlEmail_ShouldCallSesClientWithHtmlBody() {
        // Arrange
        String toEmail = "teacher@example.com";
        String subject = "HTML Test";
        String htmlBody = "<html><body><h1>Hello</h1><p>This is HTML</p></body></html>";
        String expectedMessageId = "html-message-id";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId(expectedMessageId)
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        String result = emailService.sendHtmlEmail(toEmail, subject, htmlBody);

        // Assert
        assertEquals(expectedMessageId, result);

        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals(toEmail, capturedRequest.destination().toAddresses().get(0));
        assertEquals(subject, capturedRequest.message().subject().data());
        assertEquals(htmlBody, capturedRequest.message().body().html().data());
        assertNull(capturedRequest.message().body().text());
    }

    @Test
    void sendHtmlEmail_WhenSesThrowsException_ShouldThrowRuntimeException() {
        // Arrange
        String toEmail = "test@example.com";
        String subject = "Test";
        String htmlBody = "<html><body>Test</body></html>";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailService.sendHtmlEmail(toEmail, subject, htmlBody);
        });

        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendEmail_ShouldUseUtf8Charset() {
        // Arrange
        String toEmail = "teacher@example.com";
        String subject = "Test med åäö";
        String body = "Innehåll med svenska tecken åäö ÅÄÖ";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("msg-123")
                .build();

        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        // Act
        emailService.sendEmail(toEmail, subject, body);

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals("UTF-8", capturedRequest.message().subject().charset());
        assertEquals("UTF-8", capturedRequest.message().body().text().charset());
    }
}

