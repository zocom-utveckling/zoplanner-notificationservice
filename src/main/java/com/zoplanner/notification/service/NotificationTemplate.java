package com.zoplanner.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplate {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplate.class);

    public String buildGenericMessage(String name, String message) {
        log.info("Building generic message");
        return "Hej " + name + ",\n\n" +
                message + "\n\n" +
                "Vänliga hälsningar,\nNotifikationstjänsten";
    }

    public String buildAssignmentCreatedMessage(String name, String assignmentTitle) {
        return buildGenericMessage(name, "Ett nytt uppdrag har skapats: " + assignmentTitle);
    }

    public String buildAssignmentUpdatedMessage(String name, String assignmentTitle) {
        return buildGenericMessage(name, "Uppdraget \"" + assignmentTitle + "\" har uppdaterats.");
    }

    public String buildAssignmentDeletedMessage(String name, String assignmentTitle) {
        return buildGenericMessage(name, "Uppdraget \"" + assignmentTitle + "\" har tagits bort.");
    }
}
