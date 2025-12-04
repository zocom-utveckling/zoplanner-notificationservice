package com.zoplanner.notification.dto;

import lombok.*;
// Enkel DTO för att kunna köra tester, behöver byggas ut med en riktig framöver.
@Data

public class NotificationDTO {
    private String message;
    private String recipient;

    // new fields for assignment notifications. (issue7)
    private String recipientName;  // name of the recipient.
    private String assignmentTitle; // name of the assignment.
}
