package com.zoplanner.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoplanner.notification.dto.BroadcastNotificationDTO;
import com.zoplanner.notification.event.broadcast.BroadcastNotificationEvent;
import com.zoplanner.notification.service.SqsMessagePublisher;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class BroadcastNotificationController {

    private final SqsMessagePublisher sqsMessagePublisher;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue.url}")
    private String queueUrl;

    public BroadcastNotificationController(SqsMessagePublisher sqsMessagePublisher, ObjectMapper objectMapper) {
        this.sqsMessagePublisher = sqsMessagePublisher;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@Valid @RequestBody BroadcastNotificationDTO dto) {
        log.info("Request to send broadcast notification received");
        log.debug("DTO: {}", dto);

        try {
            BroadcastNotificationEvent event = new BroadcastNotificationEvent(
                    "BROADCAST_NOTIFICATION",
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
                    dto.getManagerId(),
                    dto.getRecipientGroup(),
                    dto.getRecipientEmails(),
                    dto.getSubject(),
                    dto.getMessage(),
                    dto.getEmailType()
            );

            String json = objectMapper.writeValueAsString(event);

            sqsMessagePublisher.publishMessage(queueUrl, json);

            log.info("Broadcast notification published to SQS successfully");
            return ResponseEntity.accepted().build();

        } catch (Exception e) {
            log.error("Error sending broadcast notification", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}