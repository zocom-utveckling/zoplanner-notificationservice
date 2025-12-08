package com.zoplanner.notification.model;

/**
 * Enkel in-memory Notification-modell.
 * Ingen JPA / databas, bara en vanlig POJO.
 */
public class Notification {

    private Long id;          // kan användas senare om ni vill
    private String message;
    private String recipient;

    public Notification() {
    }

    public Notification(String message, String recipient) {
        this.message = message;
        this.recipient = recipient;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }
}
