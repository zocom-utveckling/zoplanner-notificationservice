package com.zoplanner.notification.model;

public class ConsultantSettings {
    private final String consultantId;
    private final String email;
    private final NotificationPreference preference;

    public ConsultantSettings(String consultantId, String email, NotificationPreference preference) {
        this.consultantId = consultantId;
        this.email = email;
        this.preference = preference;
    }


    public String getConsultantId() {
        return consultantId;
    }

    public String getEmail() {
        return email;
    }

    public NotificationPreference getPreference() {
        return preference;
    }
}
