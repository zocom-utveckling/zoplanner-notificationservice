package com.zoplanner.notification.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service

public class NotificationAuditLogger {

    private final Path logFilePath;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Path hämtas från application.properties
    public NotificationAuditLogger(
            @Value("${notification.audit.log-file:/app/notification-logs/notifications.log}")
            String logFile
    ) {
        this.logFilePath = Path.of(logFile);
        ensureLogDirectoryExists();
    }
    // Skapar loggfilens katalog om den inte redan finns
    private void ensureLogDirectoryExists() {
        try {
            Path dir = logFilePath.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            log.error("Failed to create log directory", e);
        }
    }

    /**
     * Loggar att ett meddelande skickades till en viss kanal
     * @param recipient = meddelandemottagare
     * @param channel = t.ex SMS, EMAIL
     * @param eventType = t.ex nytt uppdrag, uppdatering i schemat
     * @param success = om det utskicket lyckades eller inte
     */
    public void logNotificationSent(String recipient, String channel, String eventType, boolean success) {
        String timestamp = LocalDateTime.now().format(formatter);

        String line = String.format(
                "{\"timestamp\":\"%s\",\"recipient\":\"%s\",\"channel\":\"%s\",\"eventType\":\"%s\",\"success\":%s}%n",
                timestamp,
                recipient,
                channel,
                eventType,
                success
        );
        try {
            Files.writeString(
                    logFilePath,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.error("Failed to write notification to file", e);
        }
    }
}
