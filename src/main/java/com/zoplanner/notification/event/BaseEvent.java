package com.zoplanner.notification.event;

// Läser eventType utan att bry sig om resten av payload
public record BaseEvent(
        String eventType
) {
}
