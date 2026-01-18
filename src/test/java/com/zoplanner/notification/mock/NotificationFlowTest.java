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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
public class NotificationFlowTest {

    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private SnsNotificationDispatcher dispatcher;

    @Mock
    private ScheduleUpdateConsumer consumer;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(dispatcher, "topicArn", "arn:aws:sns:eu-north-1:000000000000:test-topic");
    }

    @Test
    public void testPerJob24hAndWeeklySummary() {
        ScheduleUpdateEvent perJobEvent = new ScheduleUpdateEvent();
        perJobEvent.setTeacherId("t1");
        perJobEvent.setTeacherEmail("teacher1@example.com");
        perJobEvent.setEventTime(Instant.now().plusSeconds(3600 * 24)); // 24h framåt
        perJobEvent.setPreference(NotificationPreference.PER_JOB_24H);
        perJobEvent.setCreatedAt(LocalDateTime.now());
        perJobEvent.setChanges(Collections.emptyList());

        ScheduleUpdateEvent weeklyEvent = new ScheduleUpdateEvent();
        weeklyEvent.setTeacherId("t2");
        weeklyEvent.setTeacherEmail("teacher2@example.com");
        weeklyEvent.setEventTime(Instant.now());
        weeklyEvent.setPreference(NotificationPreference.WEEKLY_SUMMARY);
        weeklyEvent.setCreatedAt(LocalDateTime.now());
        weeklyEvent.setChanges(Collections.emptyList());

        dispatcher.send24hReminder(perJobEvent); // Skickar reminder för 24h innan event
        List<ScheduleUpdateEvent> events = List.of();
        dispatcher.sendWeeklySummary(weeklyEvent.getTeacherEmail()); // Skickar veckosammanfattning
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(2)).publish(captor.capture());
        List<PublishRequest> requests = captor.getAllValues();
        PublishRequest perJobRequest = requests.get(0);
        assertEquals("arn:aws:sns:eu-north-1:000000000000:test-topic", perJobRequest.topicArn());
        assertEquals("Jobb påminnelse", perJobRequest.subject());
        assertTrue(perJobRequest.message().contains("Hej! Du har ett jobb planerat vid"));

        PublishRequest weeklyRequest = requests.get(1);
        assertEquals("arn:aws:sns:eu-north-1:000000000000:test-topic", weeklyRequest.topicArn());
        assertEquals("Veckoschema", weeklyRequest.subject());
        assertTrue(weeklyRequest.message().contains("veckosammanfattning"));


        System.out.println(weeklyRequest.subject());
        System.out.println(weeklyRequest.message());

        System.out.println(perJobRequest.subject());
        System.out.println(perJobRequest.message());
}

}

