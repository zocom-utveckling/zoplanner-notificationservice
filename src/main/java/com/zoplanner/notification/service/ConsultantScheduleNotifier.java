package com.zoplanner.notification.service;


import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;

import java.time.Instant;
import java.time.ZoneOffset;


public class ConsultantScheduleNotifier {

    private final EventBridgeClient eventBridgeClient;


    public ConsultantScheduleNotifier(EventBridgeClient eventBridgeClient) {
        this.eventBridgeClient = eventBridgeClient;
    }

    public String scheduleConsultantNotification(
            String consultantId,
            Instant notificationTime,
            String message

    ){
        String cron = toCron(notificationTime);

        PutRuleRequest request  = PutRuleRequest.builder()
                .name("consultant-notification-" + consultantId)
                .description(message)
                .scheduleExpression("cron(" + cron + ")")
                .build();

        PutRuleResponse response = eventBridgeClient.putRule(request);
        return  response.ruleArn();


    }
    private String toCron(Instant instant) {
        var t = instant.atZone(ZoneOffset.UTC);
        return String.format("%d %d %d %d ? %d",
                t.getMinute(),
                t.getHour(),
                t.getDayOfMonth(),
                t.getMonthValue(),
                t.getYear());
    }
}
