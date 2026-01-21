package com.zoplanner.notification.service;


import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.Target;

import java.time.Instant;
import java.time.ZoneOffset;


public class ConsultantScheduleNotifier {

    private final EventBridgeClient eventBridgeClient;
    private final String snsTopicArn;


    public ConsultantScheduleNotifier(EventBridgeClient eventBridgeClient, String snsTopicArn) {
        this.eventBridgeClient = eventBridgeClient;
        this.snsTopicArn = snsTopicArn;
    }

    public String scheduleConsultantNotification(
            String consultantId,
            Instant notificationTime,
            String message

    ){
        String ruleName = "consultant-notification-" + consultantId;
        String cron = toCron(notificationTime);

        PutRuleRequest request  = PutRuleRequest.builder()
                .name(ruleName)
                .description(message)
                .scheduleExpression("cron(" + cron + ")")
                .build();

        PutRuleResponse response = eventBridgeClient.putRule(request);

        PutTargetsRequest targetsRequest = PutTargetsRequest.builder()
                .rule(ruleName)
                .targets(
                        Target.builder()
                                .id("snsTarget-" + consultantId)
                                .arn(snsTopicArn)
                                .input("{\"message\": \"" + message + "\"}")
                                .build()
                )
                .build();
        eventBridgeClient.putTargets(targetsRequest);

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
