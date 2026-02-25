package com.zoplanner.notification.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.notification.NotificationPublisher;
import com.zoplanner.notification.notification.SnsNotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsPollerEmailFlowTest {

    @Mock private SqsClient sqsClient;
    @Mock private SnsClient snsClient;
    @Mock private MessageSource messageSource;

    private ObjectMapper objectMapper;
    private SqsPoller poller;

    private static final String QUEUE_URL = "https://sqs.eu-north-1.amazonaws.com/123456789012/test-queue";
    private static final String EMAIL_TOPIC_ARN = "arn:aws:sns:eu-north-1:123456789012:zoplanner-email-topic";
    private static final String EVENTS_TOPIC_ARN = "arn:aws:sns:eu-north-1:123456789012:zoplanner-events-topic";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        NotificationPublisher publisher =
                new SnsNotificationPublisher(snsClient, objectMapper, EMAIL_TOPIC_ARN, EVENTS_TOPIC_ARN, messageSource);

        // pollingEnabled=true och waitTimeSeconds=0 för snabb testkörning
        poller = new SqsPoller(
                sqsClient,
                objectMapper,
                publisher,
                QUEUE_URL,
                true,
                10,
                0
        );
    }

    @Test
    void poll_WhenScheduleUpdatedJsonArrives_ShouldPublishToSnsEmailTopic_AndDeleteMessage() {
        // Arrange
        String scheduleUpdatedJson = """
                {
                  "eventType": "SCHEDULE_UPDATED",
                  "teacherId": "teacher-1",
                  "teacherEmail": "teacher1@example.com",
                  "source": "webapi",
                  "preference": "PER_JOB_24H",
                  "eventTime": "2026-02-21T10:00:00Z",
                  "createdAt": "2026-02-20T09:30:00",
                  "message": "Du har fått ett nytt pass",
                  "changes": []
                }
                """;

        Message sqsMessage = Message.builder()
                .messageId("msg-1")
                .receiptHandle("rh-1")
                .body(scheduleUpdatedJson)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMessage).build());

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("sns-msg-123").build());

        // Act
        poller.poll();

        // Assert: publicerar till SNS (email topic)
        ArgumentCaptor<PublishRequest> publishCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(publishCaptor.capture());

        PublishRequest req = publishCaptor.getValue();
        assertEquals(EMAIL_TOPIC_ARN, req.topicArn());
        assertEquals("SCHEDULE_UPDATED", req.subject());
        assertNotNull(req.message());
        assertTrue(req.message().contains("Ditt schema har uppdaterats."));

        assertEquals("teacher-1", req.messageAttributes().get("teacherId").stringValue());
        assertEquals("SCHEDULE_UPDATED", req.messageAttributes().get("eventType").stringValue());
        assertEquals("PER_JOB_24H", req.messageAttributes().get("preference").stringValue());

        // Assert: tar bort meddelandet från SQS (undvik retry-loop)
        ArgumentCaptor<DeleteMessageRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient, times(1)).deleteMessage(deleteCaptor.capture());

        assertEquals(QUEUE_URL, deleteCaptor.getValue().queueUrl());
        assertEquals("rh-1", deleteCaptor.getValue().receiptHandle());
    }

    @Test
    void poll_WhenSnsEnvelopeWrapsScheduleUpdated_ShouldUnwrapPublishAndDelete() {
        // Arrange: typiskt SNS->SQS envelope
        String envelopeJson = """
                {
                  "Type": "Notification",
                  "TopicArn": "arn:aws:sns:eu-north-1:123456789012:events-topic",
                  "Timestamp": "2026-02-21T10:00:00Z",
                  "Message": "{\\"eventType\\":\\"SCHEDULE_UPDATED\\",\\"teacherId\\":\\"teacher-2\\",\\"teacherEmail\\":\\"teacher2@example.com\\",\\"source\\":\\"webapi\\",\\"preference\\":\\"PER_JOB_24H\\",\\"eventTime\\":\\"2026-02-21T10:00:00Z\\",\\"createdAt\\":\\"2026-02-20T09:30:00\\",\\"message\\":\\"Ändring i schema\\",\\"changes\\":[]}"
                }
                """;

        Message sqsMessage = Message.builder()
                .messageId("msg-2")
                .receiptHandle("rh-2")
                .body(envelopeJson)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(sqsMessage).build());

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("sns-msg-456").build());

        // Act
        poller.poll();

        // Assert
        ArgumentCaptor<PublishRequest> publishCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(publishCaptor.capture());

        PublishRequest req = publishCaptor.getValue();
        assertEquals("teacher-2", req.messageAttributes().get("teacherId").stringValue());

        ArgumentCaptor<DeleteMessageRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(deleteCaptor.capture());
        assertEquals("rh-2", deleteCaptor.getValue().receiptHandle());
    }
}