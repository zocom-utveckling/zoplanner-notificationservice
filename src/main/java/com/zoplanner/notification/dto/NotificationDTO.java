package com.zoplanner.notification.dto;

import lombok.*;
// Enkel DTO för att kunna köra tester, behöver byggas ut med en riktig framöver.
@Data

public class NotificationDTO {
    private String message;
    private String recipient;

    private Long id;
    private Long userId;
    private Boolean isRead;         // Visar om det är läst
    private String createdAt;
}
