package com.zoplanner.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO used when a manager wants to send a notification to a group of consultants (ex. "AllMyConsultants").
 */
public class BroadcastNotificationDTO {

    @NotBlank(message = "Manager ID is required")
    private String managerId;

    @NotBlank(message = "Recipient group is required")
    private String recipientGroup; // ex. "AllMyConsultants"

    @NotEmpty(message = "At least one recipient email is required")
    private List<String> recipientEmails;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private String channel = "EMAIL"; // default channel


    private EmailType emailType = EmailType.HTML;

    public BroadcastNotificationDTO() {}

    public BroadcastNotificationDTO(
            String managerId,
            String recipientGroup,
            List<String> recipientEmails,
            String subject,
            String message,
            String channel,
            EmailType emailType
    ) {
        this.managerId = managerId;
        this.recipientGroup = recipientGroup;
        this.recipientEmails = recipientEmails;
        this.subject = subject;
        this.message = message;
        this.channel = channel;
        this.emailType = emailType;
    }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public String getRecipientGroup() { return recipientGroup; }
    public void setRecipientGroup(String recipientGroup) { this.recipientGroup = recipientGroup; }

    public List<String> getRecipientEmails() { return recipientEmails; }
    public void setRecipientEmails(List<String> recipientEmails) { this.recipientEmails = recipientEmails; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public EmailType getEmailType() { return emailType; }
    public void setEmailType(EmailType emailType) { this.emailType = emailType; }

    // builder
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String managerId;
        private String recipientGroup;
        private List<String> recipientEmails;
        private String subject;
        private String message;
        private String channel = "EMAIL";
        private EmailType emailType = EmailType.HTML;

        private Builder() {}

        public Builder managerId(String managerId) { this.managerId = managerId; return this; }
        public Builder recipientGroup(String recipientGroup) { this.recipientGroup = recipientGroup; return this; }
        public Builder recipientEmails(List<String> recipientEmails) { this.recipientEmails = recipientEmails; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder channel(String channel) { this.channel = channel; return this; }
        public Builder emailType(EmailType emailType) { this.emailType = emailType; return this; }

        public BroadcastNotificationDTO build() {
            return new BroadcastNotificationDTO(
                    managerId,
                    recipientGroup,
                    recipientEmails,
                    subject,
                    message,
                    channel,
                    emailType
            );
        }
    }
}