package com.zoplanner.notification.mock;

import com.zoplanner.notification.service.ConsultantScheduleNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleNotifierTest {

    @Mock
    EventBridgeClient eventBridgeClient;

    @InjectMocks
    ConsultantScheduleNotifier notifier;

    @Captor
    ArgumentCaptor<PutRuleRequest> captor;

    @Test
    public void scheduleFutureNotificationSuccessfully(){

        //Arrange
        PutRuleResponse mockResponse = PutRuleResponse.builder()
                .ruleArn("arn:aws:events:eu-west-1:123456789012:rule/consultant-notification-42")
                .build();
        when(eventBridgeClient.putRule(any(PutRuleRequest.class)))
                    .thenReturn(mockResponse);
        Instant notificationTime = Instant.parse("2025-12-01T12:00:00Z");

        //Act
        String result = notifier.scheduleConsultantNotification(
                "42",
                notificationTime,
                "Notifier for work"
        );

        //Assert

        assertEquals(mockResponse.ruleArn(),result);

        verify(eventBridgeClient).putRule(captor.capture());
        PutRuleRequest sentRequest = captor.getValue();

        assertEquals("consultant-notification-42", sentRequest.name());
        assertEquals("Notifier for work", sentRequest.description());
        assertEquals("cron(0 12 1 12 ? 2025)", sentRequest.scheduleExpression());

        System.out.println(sentRequest.name());
        System.out.println(sentRequest.description());
        System.out.println(sentRequest.scheduleExpression());


    }



}
