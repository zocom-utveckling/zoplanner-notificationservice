package com.zoplanner.notification.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Läser eventType utan att bry sig om resten av payload
@JsonIgnoreProperties(ignoreUnknown = true)
public record BaseEvent(
        @JsonAlias({"eventType", "EventType"}) String eventType
) {
}
