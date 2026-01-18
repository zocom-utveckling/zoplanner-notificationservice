package com.zoplanner.notification.controller;

import com.zoplanner.notification.dto.SubscribeRequestDto;
import com.zoplanner.notification.service.SnsNotificationDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sns.SnsClient;

@RestController
@RequestMapping("/api/notification")
public class NotificationSubscriptionController {
    SnsNotificationDispatcher dispatcher;

    public NotificationSubscriptionController(SnsNotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/subscribe/email")
    public ResponseEntity<String> subscribe(@RequestBody SubscribeRequestDto dto){
        dispatcher.subscribeEmail(dto.getEmail(),dto.getTeacherId());
        return ResponseEntity.ok("Bekräftelsemejl skickat");
    }
}
