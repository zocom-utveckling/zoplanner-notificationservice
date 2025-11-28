package com.zoplanner.notification.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")

public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // Primärnyckel
    private String message;
    private String recipient;

    // För att spara när mailet skapades
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    //
    public Notification(String message, String recipient) {
        this.message = message;
        this.recipient = recipient;
    }

}
