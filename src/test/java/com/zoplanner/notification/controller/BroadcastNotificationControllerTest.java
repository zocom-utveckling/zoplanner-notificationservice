package com.zoplanner.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.BroadcastNotificationDTO;
import com.zoplanner.notification.dto.EmailType;
import com.zoplanner.notification.service.SqsMessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BroadcastNotificationControllerTest {

    @Test
    void broadcast_ShouldPublishToSqsAndReturnAccepted() {
        // arrange
        SqsMessagePublisher publisher = mock(SqsMessagePublisher.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        BroadcastNotificationController controller =
                new BroadcastNotificationController(publisher, objectMapper);

        ReflectionTestUtils.setField(controller, "queueUrl", "https://example.com/test-queue");

        BroadcastNotificationDTO dto = new BroadcastNotificationDTO();
        dto.setManagerId("m1");
        dto.setRecipientGroup("AllMyConsultants");
        dto.setRecipientEmails(List.of("a@test.com", "b@test.com"));
        dto.setSubject("Info");
        dto.setMessage("Hello consultants");
        dto.setChannel("EMAIL");
        dto.setEmailType(EmailType.HTML);

        when(publisher.publishMessage(anyString(), anyString()))
                .thenReturn("msg-123");

        // act
        ResponseEntity<Void> response = controller.broadcast(dto);

        // assert
        assertEquals(202, response.getStatusCode().value());
        verify(publisher, times(1)).publishMessage(anyString(), anyString());
    }

    @Test
    void broadcast_WhenPublisherThrows_ShouldReturn500() {
        // arrange
        SqsMessagePublisher publisher = mock(SqsMessagePublisher.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        BroadcastNotificationController controller =
                new BroadcastNotificationController(publisher, objectMapper);

        ReflectionTestUtils.setField(controller, "queueUrl", "https://example.com/test-queue");

        BroadcastNotificationDTO dto = new BroadcastNotificationDTO();
        dto.setManagerId("m1");
        dto.setRecipientGroup("AllMyConsultants");
        dto.setRecipientEmails(List.of("a@test.com"));
        dto.setSubject("Info");
        dto.setMessage("Hello");
        dto.setChannel("EMAIL");
        dto.setEmailType(EmailType.HTML);

        doThrow(new RuntimeException("boom"))
                .when(publisher).publishMessage(anyString(), anyString());

        // act
        ResponseEntity<Void> response = controller.broadcast(dto);

        // assert
        assertEquals(500, response.getStatusCode().value());
        verify(publisher, times(1)).publishMessage(anyString(), anyString());
    }
}