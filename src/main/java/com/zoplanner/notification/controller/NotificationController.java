package com.zoplanner.notification.controller;

import com.zoplanner.notification.dto.NotificationDTO;
import com.zoplanner.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> createNotification(@RequestBody NotificationDTO dto) {
        log.info("Request to create notification received");
        log.debug("DTO: {}", dto);

        try {
            notificationService.createNotification(dto);
            log.info("Notification created successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error creating notification", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // new issue8
    @PostMapping("/assignment-updated")
    public ResponseEntity<Void> assignmentUpdated(@RequestBody NotificationDTO dto) {
        log.info("Request to send 'assignment updated' notification received");
        log.debug("DTO: {}", dto);

        try {
            notificationService.sendAssignmentUpdatedNotification(dto);
            log.info("Assignment updated notification sent successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error sending assignment updated notification", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
