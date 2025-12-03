package com.zoplanner.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
// Enkel DTO för att kunna köra tester, behöver byggas ut med en riktig framöver.
@Data

public class NotificationDTO {

    @NotBlank(message = "Message cannot be empty")
    private String message; // meddelandet som skickas

    @NotBlank(message = "Recipient cannot be empty")
    @Email(message = "Invalid email address")
    private String recipient; // mottagarens epost eller telefonnummer

    @NotBlank(message = "Channel cannot be empty")
    private String channel; // email eller sms

    @NotBlank(message = "EventType cannot be empty")
    private String eventType; // uppdrag eller schemat
}
