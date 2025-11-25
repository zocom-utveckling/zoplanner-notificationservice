package com.zoplanner.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class NotificationEventLogger {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventLogger.class);

    // Create a new requestId and log that we received an event
    public String logEventReceived(String eventId) {
        String requestId = UUID.randomUUID().toString();
        log.info("Received event. requestId={}, eventId={}", requestId, eventId);
        return requestId;
    }

    // Log that the event was processed
    public void logEventProcessed(String requestId, String eventId) {
        log.info("Processed event. requestId={}, eventId={}", requestId, eventId);
    }

    // Log that the event failed
    public void logEventFailed(String requestId, String eventId, Exception ex) {
        log.error("Failed to process event. requestId={}, eventId={}", requestId, eventId, ex);
    }
}
