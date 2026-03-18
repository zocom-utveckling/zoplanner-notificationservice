package com.zoplanner.notification.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DlqPoller {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String dlqUrl;
    private final boolean pollingEnabled;
    private final int maxMessages;
    private final int waitTimeSeconds;

    // Logga bara första gången per eventId  samt fallback om eventId saknas.
    private final Set<String> loggedEventIds = ConcurrentHashMap.newKeySet();
    private final Set<String> loggedMessageIds = ConcurrentHashMap.newKeySet();

    public DlqPoller(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.dlq.url:}") String dlqUrl,
            @Value("${aws.sqs.dlq.polling.enabled:true:true}") boolean pollingEnabled,
            @Value("${aws.sqs.max.messages:10}") int maxMessages,
            @Value("${aws.sqs.wait.time.seconds:2}") int waitTimeSeconds
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.dlqUrl = dlqUrl;
        this.pollingEnabled = pollingEnabled;
        this.maxMessages = maxMessages;
        this.waitTimeSeconds = waitTimeSeconds;
    }

    @Scheduled(fixedDelayString = "${aws.sqs.dlq.polling.delay.ms:10000}")
    public void poll() {
        if (!pollingEnabled) {
            log.debug("DLQ polling disabled (aws.sqs.dlq.polling.enabled=false).");
            return;
        }

        if (dlqUrl == null || dlqUrl.isBlank()) {
            log.warn("DLQ URL is empty (aws.sqs.dlq.url=).");
            return;
        }

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(dlqUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .waitTimeSeconds(waitTimeSeconds)
                .messageSystemAttributeNames(MessageSystemAttributeName.SENT_TIMESTAMP)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        if (messages.isEmpty()) {
            log.debug("No messages received from DLQ.");
            return;
        }

        for (Message message : messages) {
            handleDlqMessage(message);
        }
    }

    private void handleDlqMessage(Message message) {
        String rawBody = message.body();

        try {
            String payloadJson = unwrapIfSnsEnvelope(rawBody);

            JsonNode root = objectMapper.readTree(payloadJson);

            String eventType = textOrNull(root, "eventType", "EventType");
            String eventId = textOrNull(root, "eventId", "EventId");

            if (eventId != null && !eventId.isBlank()) {
                if (loggedEventIds.add(eventId)) {
                    log.error(
                            "DLQ message detected for first time. eventId={} eventType={} messageId={} SentTs={} Body={}",
                            eventId,
                            safe(eventType),
                            message.messageId(),
                            sentTimestamp(message),
                            rawBody
                    );
                } else {
                    log.debug(
                            "DLQ message already logged earlier for eventId={}. Skipping duplicate log. messageId={}",
                            eventId,
                            message.messageId()
                    );
                }
                return;
            }

            if (loggedMessageIds.add(message.messageId())) {
                log.error(
                        "DLQ message detected for first time (no eventId). eventType={} messageId={} SentTs={} Body={}",
                        safe(eventType),
                        message.messageId(),
                        sentTimestamp(message),
                        rawBody
                );
            } else {
                log.debug(
                        "DLQ message already logged earlier for messageId={}. Skipping duplicate log.",
                        message.messageId()
                );
            }

        } catch (Exception e) {
            if (loggedMessageIds.add(message.messageId())) {
                log.error(
                        "Failed to inspect DLQ message for first time. messageId={} SentTs={} Body={}",
                        message.messageId(),
                        sentTimestamp(message),
                        rawBody,
                        e
                );
            } else {
                log.debug(
                        "Failed DLQ message already logged earlier. messageId={}",
                        message.messageId()
                );
            }
        }
    }

    private String unwrapIfSnsEnvelope(String body) {
        try {
            if (body != null && body.contains("\"Type\"") && body.contains("\"Message\"")) {
                SnsEnvelope env = objectMapper.readValue(body, SnsEnvelope.class);

                if (env != null && env.message() != null) {
                    String msg = env.message().trim();

                    if (msg.startsWith("{")) {
                        return msg;
                    }

                    if (msg.startsWith("\"") && msg.endsWith("\"")) {
                        String unescaped = objectMapper.readValue(msg, String.class);
                        if (unescaped != null && unescaped.trim().startsWith("{")) {
                            return unescaped;
                        }
                    }
                    return msg;
                }
            }
        } catch (Exception ignored) {
        }
        return body;
    }

    private String textOrNull(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode child = node.get(fieldName);
            if (child != null && !child.isNull()) {
                String value = child.asText();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }
        return null;
    }

    private String sentTimestamp(Message message) {
        return message.attributesAsStrings()
                .getOrDefault(MessageSystemAttributeName.SENT_TIMESTAMP.toString(), "unknown");
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
