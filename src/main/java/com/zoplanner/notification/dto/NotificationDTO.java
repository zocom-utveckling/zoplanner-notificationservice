package com.zoplanner.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object för notifikationer.
 * Innehåller all information som behövs för att skicka en notifikation via olika kanaler.
 */
@Data
public class NotificationDTO {

    // Grundläggande notifikationsfält
    @NotBlank(message = "Message cannot be empty")
    private String message;   // meddelandet som skickas

    @NotBlank(message = "Recipient cannot be empty")
    @Email(message = "Invalid email address")
    private String recipient; // mottagarens e-post

    @NotBlank(message = "Channel cannot be empty")
    private String channel;   // t.ex. EMAIL, SMS

    @NotBlank(message = "EventType cannot be empty")
    private String eventType; // t.ex. ASSIGNMENT_CREATED, SCHEDULE_UPDATED

    // E-postspecifika fält (optional – används vid email-sending)
    private String subject;
    private String emailBody;
    private EmailType emailType;
  
      // New fields for assignment notifications (issue #7)
    private String recipientName;    // name of the recipient
    private String assignmentTitle;  // title of the assignment


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
