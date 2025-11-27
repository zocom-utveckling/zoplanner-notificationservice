package com.zoplanner.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component

public class NotificationTemplate {

    // Simple generic template that we can use everywhere.
    public String buildGenericMessage(String name, String message) {
        log.info("Building generic message"); // Show that template generation started
        log.debug("Name: {}, Message: {}", name, message); // Show what data was used
        return "Hej " + name + ",\n\n"
                + message + "\n\n"
                + "Vänliga hälsningar,\n"
                + "Notifikationstjänsten";
    }

    // Template that we can use when an assignment is created.
    public String buildAssignmentCreatedMessage(String name, String assignmentTitle) {
        log.info("Building assignment created message"); // Monotoring
        log.debug("Name: {}, Assignment title: {}", name, assignmentTitle); // Troubleshooting details
        String message = "Ett nytt uppdrag har skapats: " + assignmentTitle + ".";
        return buildGenericMessage(name, message);
    }

    // Template that we can use when an assignment is updated.
    public String buildAssignmentUpdatedMessage(String name, String assignmentTitle) {
        log.info("Building assignment updated message"); // Monitoring
        log.debug("Name: {}, Assignment title: {}", name, assignmentTitle); // Troubleshooting details
        String message = "Uppdraget \"" + assignmentTitle + "\" har uppdaterats.";
        return buildGenericMessage(name, message);
    }

    // Template that we can use when an assignment is deleted.
    public String buildAssignmentDeletedMessage(String name, String assignmentTitle) {
        log.info("Building assignment deleted message"); // Monitoring
        log.debug("Name: {}, Assignment title: {}", name, assignmentTitle); // Troubleshooting details
        String message = "Uppdraget \"" + assignmentTitle + "\" har tagits bort.";
        return buildGenericMessage(name, message);
    }
}
