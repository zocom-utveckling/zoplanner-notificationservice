package com.zoplanner.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Email Service för att skicka e-post via AWS SES
 * Hanterar utskick av enkla text-emails och HTML-emails
 */
@Slf4j
@Service
public class EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.from.email}")
    private String fromEmail;

    @Value("${aws.ses.from.name}")
    private String fromName;

    @Value("${aws.ses.configuration.set:}")
    private String configurationSet;

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    /**
     * Skickar ett enkelt text-email via AWS SES
     *
     * @param toEmail Mottagarens e-postadress
     * @param subject Ämnesrad
     * @param body E-postens innehåll (plain text)
     * @return MessageId från SES om det lyckas
     * @throws SesException om något går fel vid utskick
     */
    public String sendEmail(String toEmail, String subject, String body) {
        log.info("Preparing to send email to: {}", toEmail);
        log.debug("Email subject: {}, body length: {}", subject, body.length());

        try {
            // Bygg destination (mottagare)
            Destination destination = Destination.builder()
                    .toAddresses(toEmail)
                    .build();

            // Bygg email content
            Content subjectContent = Content.builder()
                    .data(subject)
                    .charset("UTF-8")
                    .build();

            Content bodyContent = Content.builder()
                    .data(body)
                    .charset("UTF-8")
                    .build();

            Body emailBody = Body.builder()
                    .text(bodyContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(emailBody)
                    .build();

            // Bygg send request
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(String.format("%s <%s>", fromName, fromEmail));

            // Lägg till configuration set om det finns
            if (configurationSet != null && !configurationSet.isEmpty()) {
                requestBuilder.configurationSetName(configurationSet);
            }

            SendEmailRequest emailRequest = requestBuilder.build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            String messageId = response.messageId();

            log.info("Email sent successfully. MessageId: {}", messageId);
            return messageId;

        } catch (SesException e) {
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Failed to send email to: {}. Error: {}", toEmail, errorMessage, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Skickar ett HTML-formaterat email via AWS SES
     *
     * @param toEmail Mottagarens e-postadress
     * @param subject Ämnesrad
     * @param htmlBody E-postens innehåll (HTML)
     * @return MessageId från SES om det lyckas
     * @throws SesException om något går fel vid utskick
     */
    public String sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        log.info("Preparing to send HTML email to: {}", toEmail);
        log.debug("Email subject: {}, HTML body length: {}", subject, htmlBody.length());

        try {
            Destination destination = Destination.builder()
                    .toAddresses(toEmail)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .charset("UTF-8")
                    .build();

            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .charset("UTF-8")
                    .build();

            Body emailBody = Body.builder()
                    .html(htmlContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(emailBody)
                    .build();

            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(String.format("%s <%s>", fromName, fromEmail));

            if (configurationSet != null && !configurationSet.isEmpty()) {
                requestBuilder.configurationSetName(configurationSet);
            }

            SendEmailRequest emailRequest = requestBuilder.build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            String messageId = response.messageId();

            log.info("HTML email sent successfully. MessageId: {}", messageId);
            return messageId;

        } catch (SesException e) {
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Failed to send HTML email to: {}. Error: {}", toEmail, errorMessage, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending HTML email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
}

