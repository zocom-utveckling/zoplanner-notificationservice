package com.zoplanner.notification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDTO {

    @NotBlank(message = "Recipient cannot be empty")   // <-- FIX #1
    @Email(message = "Recipient must be a valid email address")
    private String recipient;

    @NotBlank(message = "Channel is required")
    private String channel;

    @NotBlank(message = "Event type is required")
    @JsonAlias({"EventType", "eventType"})
    private String eventType;

    private String subject;

    @NotBlank(message = "Message cannot be empty")     // <-- FIX #2
    private String message;

    private String emailBody;
    private boolean htmlEmail;

    private EmailType emailType;

    public NotificationDTO() {}

    public NotificationDTO(String recipient,
                           String channel,
                           String eventType,
                           String subject,
                           String message,
                           String emailBody,
                           boolean htmlEmail,
                           EmailType emailType) {
        this.recipient = recipient;
        this.channel = channel;
        this.eventType = eventType;
        this.subject = subject;
        this.message = message;
        this.emailBody = emailBody;
        this.htmlEmail = htmlEmail;
        this.emailType = emailType;
    }

    // getters & setters…

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }

    public boolean isHtmlEmail() { return htmlEmail; }
    public boolean getHtmlEmail() { return htmlEmail; }
    public void setHtmlEmail(boolean htmlEmail) { this.htmlEmail = htmlEmail; }

    public EmailType getEmailType() { return emailType; }
    public void setEmailType(EmailType emailType) { this.emailType = emailType; }

    // builder:

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String recipient;
        private String channel;
        private String eventType;
        private String subject;
        private String message;
        private String emailBody;
        private boolean htmlEmail;
        private EmailType emailType;

        private Builder() {}

        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder channel(String channel) { this.channel = channel; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder emailBody(String emailBody) { this.emailBody = emailBody; return this; }
        public Builder htmlEmail(boolean htmlEmail) { this.htmlEmail = htmlEmail; return this; }
        public Builder emailType(EmailType emailType) { this.emailType = emailType; return this; }

        public NotificationDTO build() {
            return new NotificationDTO(
                    recipient,
                    channel,
                    eventType,
                    subject,
                    message,
                    emailBody,
                    htmlEmail,
                    emailType
            );
        }
    }
}
