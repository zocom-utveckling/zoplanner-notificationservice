package com.zoplanner.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object för notifikationer
 * Innehåller all information som behövs för att skicka en notifikation via olika kanaler
 */
@Data
public class NotificationDTO {

    // Grundläggande notifikationsfält
    @NotBlank(message = "Message cannot be empty")
    private String message; // meddelandet som skickas

    @NotBlank(message = "Recipient cannot be empty")
    @Email(message = "Invalid email address")
    private String recipient; // mottagarens epost eller telefonnummer

    @NotBlank(message = "Channel cannot be empty")
    private String channel; // email eller sms

    @NotBlank(message = "EventType cannot be empty")
    private String eventType; // uppdrag eller schemat

    // E-postspecifika fält (optional - används vid email-sending)
    private String subject;
    private String emailBody;
    private EmailType emailType;
  
      // New fields for assignment notifications (issue #7)
    private String recipientName;    // name of the recipient
    private String assignmentTitle;  // title of the assignment

}
