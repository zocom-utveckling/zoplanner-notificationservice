package com.zoplanner.notification.dto;

import lombok.*;

/**
 * Data Transfer Object för notifikationer
 * Innehåller all information som behövs för att skicka en notifikation via olika kanaler
 */
@Data
public class NotificationDTO {
    // Grundläggande notifikationsfält
    private String message;
    private String recipient;

    // E-postspecifika fält
    private String subject;
    private String emailBody;
    private EmailType emailType;
}
