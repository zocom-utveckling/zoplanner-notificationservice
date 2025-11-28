package com.zoplanner.notification.model;

import lombok.*;

// Enkel Notification modell för att kunna köra tester, behöver ändras till en riktig framöver.

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Notification {

    private Long id;
    private String message;
    private String recipient;

    private Long userId;
    private boolean isRead;
    private String createdAt;

    public Notification(String message, String recipient) {
        this.message = message;
        this.recipient = recipient;
        this.isRead = false;  // Nya notifikationer är olästa som standard
    }

    // Konstruktor för att skapa notifikationer med userId
    public Notification(String message, Long userId) {
        this.message = message;
        this.userId = userId;
        this.isRead = false;
    }

}
