package com.zoplanner.notification.controller;


import com.zoplanner.notification.dto.EmailRequest;
import com.zoplanner.notification.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Request to send email to: {}", emailRequest.getRecipient());
        log.debug("Email request details: {}", emailRequest);

        try {
            String messageId;


            if (emailRequest.isHtml()) {
                // Send HTML email
                messageId = emailService.sendHtmlEmail(
                        emailRequest.getRecipient(),
                        emailRequest.getSubject(),
                        emailRequest.getBody()
                );
                log.info("HTML email sent successfully with MessageId: {}", messageId);
            } else {
                // Send normal text email
                messageId = emailService.sendEmail(
                        emailRequest.getRecipient(),
                        emailRequest.getSubject(),
                        emailRequest.getBody()
                );
                log.info("Text email sent successfully with MessageId: {}", messageId);
            }

            Map<String, String> response = new HashMap<>();
            response.put("Status", "Success");
            response.put("Message", "Email sent successfully");
            response.put("MessageId", messageId);  // Message ID for tracking
            response.put("recipient", emailRequest.getRecipient());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailRequest.getRecipient(), e.getMessage(), e); // If email sending fails - log and return error

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send email: " + e.getMessage());
            errorResponse.put("recipient", emailRequest.getRecipient());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Health check endpoint to verify that the EmailController is active
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "EmailController");
        return ResponseEntity.ok(response);
    }


}

