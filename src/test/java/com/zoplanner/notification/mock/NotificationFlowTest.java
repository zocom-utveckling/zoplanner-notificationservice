package com.zoplanner.notification.mock;

import com.zoplanner.notification.consumer.ScheduleUpdateConsumer;
import com.zoplanner.notification.event.ScheduleUpdateEvent;
import com.zoplanner.notification.model.NotificationPreference;
import com.zoplanner.notification.service.SnsNotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
public class NotificationFlowTest {

    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private SnsNotificationDispatcher dispatcher;


    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(dispatcher, "topicArn", "arn:aws:sns:eu-north-1:084828590879:Zoplanner");
    }

    @Test
    public void testPerJob24hAndWeeklySummary() {
        // Arrange
        ScheduleUpdateEvent perJobEvent = new ScheduleUpdateEvent();
        perJobEvent.setTeacherId("teacher1");
        perJobEvent.setTeacherEmail("teacher1@example.com");
        perJobEvent.setEventTime(Instant.now().plusSeconds(3600 * 25));
        perJobEvent.setPreference(NotificationPreference.PER_JOB_24H);
        perJobEvent.setCreatedAt(Instant.now());
        perJobEvent.setChanges(Collections.emptyList());

        // Flera events för weekly summary
        ScheduleUpdateEvent weeklyEvent1 = new ScheduleUpdateEvent();
        weeklyEvent1.setTeacherId("teacher2");
        weeklyEvent1.setTeacherEmail("teacher2@example.com");
        weeklyEvent1.setEventTime(Instant.parse("2026-02-03T09:00:00Z"));
        weeklyEvent1.setPreference(NotificationPreference.WEEKLY_SUMMARY);

        ScheduleUpdateEvent weeklyEvent2 = new ScheduleUpdateEvent();
        weeklyEvent2.setTeacherId("teacher2");
        weeklyEvent2.setEventTime(Instant.parse("2026-02-05T14:00:00Z"));
        weeklyEvent2.setPreference(NotificationPreference.WEEKLY_SUMMARY);

        List<ScheduleUpdateEvent> weeklyEvents = List.of(weeklyEvent1, weeklyEvent2);

        // Act
        dispatcher.send24hReminder(perJobEvent);
        dispatcher.sendWeeklySummary("teacher2", weeklyEvents);

        // Assert
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(2)).publish(captor.capture());
        List<PublishRequest> requests = captor.getAllValues();

        // Verifiera 24h reminder
        PublishRequest perJobRequest = requests.get(0);
        assertEquals("Jobb påminnelse", perJobRequest.subject());
        assertTrue(perJobRequest.message().contains("Hej! Du har ett jobb planerat vid"));
        assertEquals("teacher1",
                perJobRequest.messageAttributes().get("teacherId").stringValue());

        // Verifiera weekly summary
        PublishRequest weeklyRequest = requests.get(1);
        assertEquals("Veckoschema", weeklyRequest.subject());
        assertTrue(weeklyRequest.message().contains("veckosammanfattning"));
        assertTrue(weeklyRequest.message().contains("2 jobb schemalagda"),
                "Meddelandet ska innehålla antal jobb");
        assertTrue(weeklyRequest.message().contains("2026-02-03"),
                "Meddelandet ska innehålla första eventet");
        assertTrue(weeklyRequest.message().contains("2026-02-05"),
                "Meddelandet ska innehålla andra eventet");
        assertEquals("teacher2",
                weeklyRequest.messageAttributes().get("teacherId").stringValue());

        System.out.println("\n=== 24h Reminder ===");
        System.out.println("Subject: " + perJobRequest.subject());
        System.out.println("Message: " + perJobRequest.message());
        System.out.println("Teacher ID: " + perJobRequest.messageAttributes().get("teacherId").stringValue());

        System.out.println("\n=== Weekly Summary ===");
        System.out.println("Subject: " + weeklyRequest.subject());
        System.out.println("Message: " + weeklyRequest.message());
        System.out.println("Teacher ID: " + weeklyRequest.messageAttributes().get("teacherId").stringValue());
    }

    // Med nuvarande implementation publiceras inget när event-listan är tom
    @Test
    public void testWeeklySummaryWithNoEvents() {
        // Act
        assertDoesNotThrow(() -> dispatcher.sendWeeklySummary(
                "teacher123", Collections.emptyList()
        ));

        verify(snsClient, never()).publish(any(PublishRequest.class));
    }


    @Test
    public void testSubscribeEmail() {
        // Arrange
        String email = "teacher@example.com";
        String teacherId = "teacher123";

        when(snsClient.subscribe(any(SubscribeRequest.class)))
                .thenReturn(SubscribeResponse.builder()
                        .subscriptionArn("arn:aws:sns:eu-north-1:084828590879:Zoplanner:sub-123")
                        .build());

        // Act
        dispatcher.subscribeEmail(email, teacherId);

        // Assert
        ArgumentCaptor<SubscribeRequest> captor = ArgumentCaptor.forClass(SubscribeRequest.class);
        verify(snsClient, times(1)).subscribe(captor.capture());

        SubscribeRequest request = captor.getValue();
        assertEquals("arn:aws:sns:eu-north-1:084828590879:Zoplanner", request.topicArn());
        assertEquals("email", request.protocol());
        assertEquals(email, request.endpoint());

        // Verifiera filter policy
        String filterPolicy = request.attributes().get("FilterPolicy");
        assertTrue(filterPolicy.contains("teacherId"));
        assertTrue(filterPolicy.contains(teacherId));

        System.out.println("Subscribe Request:");
        System.out.println("Topic: " + request.topicArn());
        System.out.println("Email: " + request.endpoint());
        System.out.println("Filter Policy: " + filterPolicy);
    }


}

