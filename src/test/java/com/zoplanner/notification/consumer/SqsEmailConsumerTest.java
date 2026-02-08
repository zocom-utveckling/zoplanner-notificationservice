package com.zoplanner.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zoplanner.notification.dto.EmailType;
import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.handler.NewAssignmentNotificationHandler;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.NotificationService;
import com.zoplanner.notification.service.WeeklyEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för SqsEmailConsumer
 * Testar att meddelanden från SQS hanteras korrekt
 */
@ExtendWith(MockitoExtension.class)
class SqsEmailConsumerTest {

    @Mock
    private NewAssignmentNotificationHandler newAssignmentHandler;

    @Mock
    private SqsClient sqsClient;

    @Mock
    private NotificationService notificationService;

    private SqsEmailConsumer consumer;
    private ObjectMapper objectMapper;
    private final String testQueueUrl = "https://sqs.eu-north-1.amazonaws.com/123456789012/test-queue";
    private ScheduleUpdateConsumer updateConsumer;
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();


        consumer = new SqsEmailConsumer(sqsClient, notificationService, objectMapper, newAssignmentHandler,updateConsumer);

        ReflectionTestUtils.setField(consumer, "queueUrl", testQueueUrl);
        ReflectionTestUtils.setField(consumer, "pollingEnabled", true);
        ReflectionTestUtils.setField(consumer, "maxMessages", 10);
        ReflectionTestUtils.setField(consumer, "waitTimeSeconds", 20);
    }

    // För att BaseEvent routing ska fungera
    private String toLegacyMessageBody(NotificationDTO dto) throws Exception {
        ObjectNode node = objectMapper.valueToTree(dto);
        node.put("eventType", "LEGACY_NOTIFICATION");
        return objectMapper.writeValueAsString(node);
    }

    @Test
    void pollMessages_WhenMessagesExist_ShouldProcessAndDeleteThem() throws Exception {
        // Arrange
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipient("teacher@example.com");
        dto.setMessage("Test notification");
        dto.setSubject("Test Subject");
        dto.setEmailBody("Test email body");
        dto.setEmailType(EmailType.TEXT);

        String messageBody = objectMapper.writeValueAsString(dto);
        String messageId = "test-msg-123";
        String receiptHandle = "test-receipt-handle";

        Message sqsMessage = Message.builder()
                .messageId(messageId)
                .body(messageBody)
                .receiptHandle(receiptHandle)
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(sqsMessage)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);

        DeleteMessageResponse deleteResponse = DeleteMessageResponse.builder().build();
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(deleteResponse);

        // Act
        consumer.pollMessages();

        // Assert
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(notificationService).createNotification(any(NotificationDTO.class));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));

        ArgumentCaptor<NotificationDTO> dtoCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(notificationService).createNotification(dtoCaptor.capture());
        NotificationDTO capturedDto = dtoCaptor.getValue();

        assertEquals("teacher@example.com", capturedDto.getRecipient());
        assertEquals("Test notification", capturedDto.getMessage());
        assertEquals("Test Subject", capturedDto.getSubject());
        assertEquals("Test email body", capturedDto.getEmailBody());
    }

    @Test
    void pollMessages_WhenNoMessages_ShouldNotProcessAnything() {
        // Arrange
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);

        // Act
        consumer.pollMessages();

        // Assert
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(notificationService, never()).createNotification(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_WhenPollingDisabled_ShouldNotPoll() {
        // Arrange
        ReflectionTestUtils.setField(consumer, "pollingEnabled", false);

        // Act
        consumer.pollMessages();

        // Assert
        verify(sqsClient, never()).receiveMessage((ReceiveMessageRequest) any());
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void pollMessages_WhenQueueUrlIsEmpty_ShouldNotPoll() {
        // Arrange
        ReflectionTestUtils.setField(consumer, "queueUrl", "");

        // Act
        consumer.pollMessages();

        // Assert
        verify(sqsClient, never()).receiveMessage((ReceiveMessageRequest) any());
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void pollMessages_WhenSqsThrowsException_ShouldHandleGracefully() {
        // Arrange
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(SqsException.builder().message("SQS error").build());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> consumer.pollMessages());

        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void processMessage_WithInvalidJson_ShouldNotDeleteMessage() {
        // Arrange
        Message sqsMessage = Message.builder()
                .messageId("invalid-msg")
                .body("{ invalid json }")
                .receiptHandle("receipt-handle")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(sqsMessage)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);

        // Act
        consumer.pollMessages();

        // Assert
        verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        verify(notificationService, never()).createNotification(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void processMessage_WithMissingRecipient_ShouldNotDeleteMessage() throws Exception {
        // Arrange
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Test message");
        // Missing recipient

        String messageBody = objectMapper.writeValueAsString(dto);

        Message sqsMessage = Message.builder()
                .messageId("test-msg")
                .body(messageBody)
                .receiptHandle("receipt-handle")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(sqsMessage)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);

        // Act
        consumer.pollMessages();

        // Assert
        verify(notificationService, never()).createNotification(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void processMessage_WhenNotificationServiceThrowsException_ShouldNotDeleteMessage() throws Exception {
        // Arrange
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipient("teacher@example.com");
        dto.setMessage("Test notification");
        dto.setSubject("Test");
        dto.setEmailBody("Body");

        String messageBody = objectMapper.writeValueAsString(dto);

        Message sqsMessage = Message.builder()
                .messageId("test-msg")
                .body(messageBody)
                .receiptHandle("receipt-handle")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(sqsMessage)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);
        doThrow(new RuntimeException("Service error")).when(notificationService).createNotification(any());

        // Act
        consumer.pollMessages();

        // Assert
        verify(notificationService).createNotification(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_WithMultipleMessages_ShouldProcessAll() throws Exception {
        // Arrange
        NotificationDTO dto1 = new NotificationDTO();
        dto1.setRecipient("teacher1@example.com");
        dto1.setMessage("Message 1");
        dto1.setSubject("Subject 1");
        dto1.setEmailBody("Body 1");

        NotificationDTO dto2 = new NotificationDTO();
        dto2.setRecipient("teacher2@example.com");
        dto2.setMessage("Message 2");
        dto2.setSubject("Subject 2");
        dto2.setEmailBody("Body 2");

        Message msg1 = Message.builder()
                .messageId("msg-1")
                .body(objectMapper.writeValueAsString(dto1))
                .receiptHandle("receipt-1")
                .build();

        Message msg2 = Message.builder()
                .messageId("msg-2")
                .body(objectMapper.writeValueAsString(dto2))
                .receiptHandle("receipt-2")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(List.of(msg1, msg2))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());

        // Act
        consumer.pollMessages();

        // Assert
        verify(notificationService, times(2)).createNotification(any());
        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_ShouldUseCorrectSqsParameters() {
        // Arrange
        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);

        // Act
        consumer.pollMessages();

        // Assert
        ArgumentCaptor<ReceiveMessageRequest> requestCaptor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        verify(sqsClient).receiveMessage(requestCaptor.capture());

        ReceiveMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals(testQueueUrl, capturedRequest.queueUrl());
        assertEquals(10, capturedRequest.maxNumberOfMessages());
        assertEquals(20, capturedRequest.waitTimeSeconds());
    }

    @Test
    void processMessage_WithHtmlEmail_ShouldPreserveEmailType() throws Exception {
        // Arrange
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipient("teacher@example.com");
        dto.setMessage("Test notification");
        dto.setSubject("HTML Test");
        dto.setEmailBody("<html><body>HTML content</body></html>");
        dto.setEmailType(EmailType.HTML);

        String messageBody = objectMapper.writeValueAsString(dto);

        Message sqsMessage = Message.builder()
                .messageId("html-msg")
                .body(messageBody)
                .receiptHandle("receipt-handle")
                .build();

        ReceiveMessageResponse receiveResponse = ReceiveMessageResponse.builder()
                .messages(sqsMessage)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveResponse);
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());

        // Act
        consumer.pollMessages();

        // Assert
        ArgumentCaptor<NotificationDTO> dtoCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
        verify(notificationService).createNotification(dtoCaptor.capture());

        NotificationDTO capturedDto = dtoCaptor.getValue();
        assertEquals(EmailType.HTML, capturedDto.getEmailType());
        assertTrue(capturedDto.getEmailBody().contains("<html>"));
    }

    @Test
    void pollMessages_WhenNewAssignmentEvent_ShouldRouteToHandlerAndDeleteMessage() {
        // Arrange
        String body = """
        {
          "eventType": "NEW_ASSIGNMENT",
          "eventId": "123e4567-e89b-12d3-a456-426614174000",
          "timestamp": "2025-01-01T10:00:00Z",
          "teacherId": "t1",
          "teacherName": "Anna Andersson",
          "teacherEmail": "anna@test.com",
          "assignmentId": "a1",
          "assignmentDescription": "Test assignment",
          "assignmentDueDate": "2025-01-31"
        }
        """;

        Message sqsMessage = Message.builder()
                .messageId("msg-new-assignment")
                .receiptHandle("rh-new-assignment")
                .body(body)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMessage).build());

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(DeleteMessageResponse.builder().build());

        //Act
        consumer.pollMessages();

        //Assert
        verify(newAssignmentHandler).handle(any(NewAssignmentEvent.class));
        verify(notificationService, never()).createNotification(any(NotificationDTO.class));
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));

    }

    @Test
    void pollMessages_WhenNewAssignmentHandlerThrows_ShouldNotDeleteMessage() {
        // Arrange
        String body = """
        {
          "eventType": "NEW_ASSIGNMENT",
          "eventId": "123e4567-e89b-12d3-a456-426614174000",
          "timestamp": "2025-01-01T10:00:00Z",
          "teacherId": "t1",
          "teacherName": "Anna Andersson",
          "teacherEmail": "anna@test.com",
          "assignmentId": "a1",
          "assignmentDescription": "Test assignment",
          "assignmentDueDate": "2025-01-31"
        }
        """;

        Message sqsMessage = Message.builder()
                .messageId("msg-new-assignment")
                .receiptHandle("rh-new-assignment")
                .body(body)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMessage).build());

        doThrow(new RuntimeException("boom"))
                .when(newAssignmentHandler)
                .handle(any(NewAssignmentEvent.class));

        // Act
        consumer.pollMessages();

        // Assert
        verify(newAssignmentHandler).handle(any(NewAssignmentEvent.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
        verify(notificationService, never()).createNotification(any(NotificationDTO.class));
    }

}

