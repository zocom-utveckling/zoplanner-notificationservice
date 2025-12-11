package com.zoplanner.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String UTF8 = "UTF-8";

    private final SesClient sesClient;

    @Value("${notification.email.from:}")
    private String fromEmail;

    @Value("${notification.email.fromName:}")
    private String fromName;

    @Value("${notification.email.configurationSet:}")
    private String configurationSet;

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    /**
     * Sends a plain text email and returns the SES message ID.
     * If SES fails, the SesException is propagated (tests expect this).
     */
    public String sendEmail(String to, String subject, String body) {
        SendEmailRequest request = buildRequest(to, subject, body, false);

        try {
            SendEmailResponse response = sesClient.sendEmail(request);
            return response.messageId();
        } catch (SesException e) {
            log.error("Error sending email via SES", e);
            throw e;
        }
    }

    /**
     * Sends an HTML email and returns the SES message ID.
     * If SES fails, a RuntimeException is thrown (tests expect this).
     */
    public String sendHtmlEmail(String to, String subject, String htmlBody) {
        SendEmailRequest request = buildRequest(to, subject, htmlBody, true);

        try {
            SendEmailResponse response = sesClient.sendEmail(request);
            return response.messageId();
        } catch (SesException e) {
            log.error("Error sending HTML email via SES", e);
            throw new RuntimeException("Failed to send HTML email via SES", e);
        }
    }

    // -------- private helper --------

    private SendEmailRequest buildRequest(String to, String subject, String body, boolean html) {
        // email part, fall back if not configured
        String emailPart = (fromEmail != null && !fromEmail.isBlank())
                ? fromEmail
                : "no-reply@example.com";

        // full "source" including optional display name
        String source;
        if (fromName != null && !fromName.isBlank()) {
            source = fromName + " <" + emailPart + ">";
        } else {
            source = emailPart;
        }

        Content subjectContent = Content.builder()
                .data(subject)
                .charset(UTF8)
                .build();

        Content bodyContent = Content.builder()
                .data(body)
                .charset(UTF8)
                .build();

        Body emailBody = html
                ? Body.builder().html(bodyContent).build()
                : Body.builder().text(bodyContent).build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(emailBody)
                .build();

        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        SendEmailRequest.Builder builder = SendEmailRequest.builder()
                .source(source)
                .destination(destination)
                .message(message);

        // Only include configuration set if configured (tests check this)
        if (configurationSet != null && !configurationSet.isBlank()) {
            builder.configurationSetName(configurationSet);
        }

        return builder.build();
    }
}
