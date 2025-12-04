package com.zoplanner.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Enkel DTO som motsvarar befintliga tester.
 */
public class NotificationDTO {

    @NotBlank(message = "Message cannot be empty")
    private String message; // meddelandet som skickas

    @NotBlank(message = "Recipient cannot be empty")
    @Email(message = "Invalid email address")
    private String recipient; // mottagarens epost

    @NotBlank(message = "Channel cannot be empty")
    private String channel; // t.ex. EMAIL eller SMS

    @NotBlank(message = "EventType cannot be empty")
    private String eventType = "GENERIC"; // t.ex. ASSIGNMENT_CREATED, SCHEDULE_UPDATED

    public NotificationDTO() {
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getChannel() {
        return channel;
    }

    public String getEventType() {
        return eventType;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "NotificationDTO{" +
                "message='" + message + '\'' +
                ", recipient='" + recipient + '\'' +
                ", channel='" + channel + '\'' +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}
