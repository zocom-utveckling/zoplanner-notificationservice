package com.zoplanner.notification.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_log")

public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; // Primärnyckel
    private String recipient; // Mottagare
    private String subject; // Ämne på email
    private String message; // Meddelande i email
    private LocalDateTime sentAt; // När email skickades
    private boolean success; // Om email skickades utan fel
    private String errorMessage; // Felmeddelande
}
