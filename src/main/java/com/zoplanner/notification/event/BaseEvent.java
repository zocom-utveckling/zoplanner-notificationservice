package com.zoplanner.notification.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaseEvent(
        @JsonProperty("eventType")
        @JsonAlias({"EventType", "event_type"})
        String eventType
) {}
