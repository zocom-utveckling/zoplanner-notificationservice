package com.zoplanner.notification.handler;

import com.zoplanner.notification.event.newassignment.NewAssignmentEvent;
import com.zoplanner.notification.service.EmailService;
import com.zoplanner.notification.service.NotificationTemplate;
import org.springframework.stereotype.Component;


// För att översätta NEW_ASSIGNMENT till mail via EmailService

@Component
public class NewAssignmentNotificationHandler {

    private final EmailService emailService;
    private final NotificationTemplate notificationTemplate;

    public NewAssignmentNotificationHandler(EmailService emailService, NotificationTemplate notificationTemplate) {
        this.emailService = emailService;
        this.notificationTemplate = notificationTemplate;
    }

    public void handle(NewAssignmentEvent event) {
        String subject = "Nytt uppdrag tilldelat";

        String body = notificationTemplate.buildAssignmentCreatedMessage(
                event.teacherName() != null ? event.teacherName() :"",
                event.assignmentDescription()
        );

        emailService.sendEmail(
                event.teacherEmail(),
                subject,
                body
        );

    }
}
