package com.zoplanner.notification.sqs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SnsEnvelope(
        @JsonProperty("Type") String type,
        @JsonProperty("Message") String message,
        @JsonProperty("TopicArn") String topicArn,
        @JsonProperty("Timestamp") String timestamp
) {}