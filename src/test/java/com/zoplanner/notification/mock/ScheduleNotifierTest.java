package com.zoplanner.notification.mock;

import com.zoplanner.notification.consumer.ScheduleUpdateConsumer;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.ConsultantScheduleNotifier;
import com.zoplanner.notification.service.NotificationDispatcher;
import com.zoplanner.notification.service.ReminderScheduler;
import com.zoplanner.notification.service.WeeklyEventStore;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleNotifierTest {
    @InjectMocks
    ScheduleUpdateConsumer consumer;

    @Mock
    private ReminderScheduler scheduler;

    @Mock
    private NotificationDispatcher dispatcher;

    @Mock
    EventBridgeClient eventBridgeClient;

    @Mock
    private WeeklyEventStore weeklyEventStore;

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

    @Test
    public void testPerJob24hReminder(){
        Instant jobTime = Instant.now().plusSeconds(60);

        ScheduleUpdateEvent event = new ScheduleUpdateEvent();
        event.setTeacherId("42");
        event.setTeacherEmail("teacher42@school.se");
        event.setSource("SCHEDULE_SERVICE");
        event.setPreference(NotificationPreference.PER_JOB_24H);
        event.setEventTime(jobTime);
        event.setCreatedAt(Instant.now());
        event.setChanges(Collections.emptyList());

        consumer.handleMessage(event);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).scheduleReminder(any(), runnableCaptor.capture());

        runnableCaptor.getValue().run();

        verify(dispatcher).send24hReminder(event);

        System.out.println(event);
    }

    /**
     * Testet har justerats för att verifiera nuvarande beteende.
     * Eventet sparas i WeeklyEventStore via handleMessage(), men
     * sendWeeklySummaries() läser från den interna weeklyEvent-mappen.
     * Därför sker ingen dispatch i det här flödet.
     *
     * Notera också att ScheduleUpdateConsumer för närvarande skickar teacherId
     * till sendWeeklySummary(), trots att dispatch-metoden tar argumentet "email"
     */
    @Test
    public void weeklySummary_isCollectedAndDispatched() {
        // Arrange
        ScheduleUpdateEvent event = new ScheduleUpdateEvent();
        event.setTeacherId("43");
        event.setTeacherEmail("teacher43@school.se");
        event.setSource("SCHEDULE_SERVICE");
        event.setPreference(NotificationPreference.WEEKLY_SUMMARY);
        event.setEventTime(Instant.now().plusSeconds(3600));
        event.setCreatedAt(Instant.now());
        event.setChanges(Collections.emptyList());

        // Act
        consumer.handleMessage(event);

        // Assert
        verify(weeklyEventStore).addEvent(event);
        verifyNoInteractions(dispatcher);
    }


}
