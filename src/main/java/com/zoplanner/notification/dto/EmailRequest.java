package com.zoplanner.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequest {

    @NotBlank(message = "Recipient email cannot be empty")
    @Email(message = "Invalid email format")
    private String recipient;

    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @NotBlank(message = "Body cannot be empty")
    private String body;

    private boolean isHtml = false;

    public EmailRequest() {
    }

    public EmailRequest(String recipient, String subject, String body, boolean isHtml) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.isHtml = isHtml;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean html) {
        this.isHtml = html;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + (body != null ? body.substring(0, Math.min(50, body.length())) : null) + '\'' +
                ", isHtml=" + isHtml +
                '}';
    }
}