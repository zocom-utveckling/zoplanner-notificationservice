package service;

public class NotificationTemplate {

    // Simple generic template that we can use everywhere.
    public String buildGenericMessage(String name, String message) {
        return "Hej " + name + ",\n\n"
                + message + "\n\n"
                + "Vänliga hälsningar,\n"
                + "Notifikationstjänsten";
    }

    // Template that we can use when an assignment is created.
    public String buildAssignmentCreatedMessage(String name, String assignmentTitle) {
        String message = "Ett nytt uppdrag har skapats: " + assignmentTitle + ".";
        return buildGenericMessage(name, message);
    }

    // Template that we can use when an assignment is updated.
    public String buildAssignmentUpdatedMessage(String name, String assignmentTitle) {
        String message = "Uppdraget \"" + assignmentTitle + "\" har uppdaterats.";
        return buildGenericMessage(name, message);
    }

    // Template that we can use when an assignment is deleted.
    public String buildAssignmentDeletedMessage(String name, String assignmentTitle) {
        String message = "Uppdraget \"" + assignmentTitle + "\" har tagits bort.";
        return buildGenericMessage(name, message);
    }
}
