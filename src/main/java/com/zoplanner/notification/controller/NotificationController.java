package com.zoplanner.notification.controller;

import com.zoplanner.notification.service.NotificationService;
import com.zoplanner.notification.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/notifications")

public class NotificationController {

    private final NotificationService notificationService;

    // Dependency injection, Spring skickar in NotificationService i konstruktorn
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> createNotification(@RequestBody NotificationDTO dto) {
        log.info("Request to create notification received"); // Övervakning, visar att ett anrop kommit
        log.debug("DTO: {}", dto); // Felsökning, visar vad som skickats in

        try {
            notificationService.createNotification(dto); // Skickar in DTO till service
            log.info("Notification created successfully"); // Övervakning, visar att flödet lyckades
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error creating notification", e); // Felsökning, visar stacktrace
            return ResponseEntity.internalServerError().build();
        }
    }
}
