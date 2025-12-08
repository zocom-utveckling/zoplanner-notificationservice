package com.zoplanner.notification.model;

import lombok.*;

// Enkel Notification modell för att kunna köra tester, behöver ändras till en riktig framöver.

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Notification {
    private String message;
    private String recipient;

}
