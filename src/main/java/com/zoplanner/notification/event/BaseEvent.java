package com.zoplanner.notification.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Läser eventType utan att bry sig om resten av payload
@JsonIgnoreProperties(ignoreUnknown = true)
public record BaseEvent(
        String eventType
) {
}
