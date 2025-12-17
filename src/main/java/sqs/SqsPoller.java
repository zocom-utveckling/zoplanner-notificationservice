package sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

@Slf4j
@Component
public class SqsPoller {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public SqsPoller(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${aws.sqs.que-url}")
            String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    @PostConstruct
    public void poll() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(20)
                .build();

        sqsClient.receiveMessage(request).messages().forEach(this::handleMessage);
    }

    private void handleMessage(Message message) {
        try {
            NewAssignmentEvent event =
                    objectMapper.readValue(message.body(), NewAssignmentEvent.class);

            log.info("Received NEW_ASSIGNMENT event: {}", event.eventId());

            /*
            Här ska det vara kod för att skicka mail
            t.ex emailService.sendNewAssignmentEmail(event);
            */

            deleteMessage(message);
        } catch (Exception e) {
            log.error("Failed to process message", e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }


}
