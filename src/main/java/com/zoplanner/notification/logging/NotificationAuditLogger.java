package com.zoplanner.notification.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(NotificationAuditLogger.class);

    private final Path logFilePath;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NotificationAuditLogger(
            @Value("${notification.audit.log-file:logs/notifications.log}")
            String logFile
    ) {
        this.logFilePath = Path.of(logFile);
        createDirectorySafely();
    }

    /**
     * Ensures the parent directory exists.
     * Never throws — prevents CI failures during contextLoads.
     */
    private void createDirectorySafely() {
        try {
            Path dir = logFilePath.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Created audit log directory: {}", dir.toAbsolutePath());
            }
        } catch (IOException e) {
            // DO NOT rethrow — CI filesystem may be read-only
            log.warn("Could not create audit log directory. Audit logging may be disabled. Reason: {}",
                    e.getMessage());
        }
    }

    /**
     * Writes an audit JSON entry into the log file.
     * If writing fails, log an error but never interrupt the application.
     */
    public void logNotificationSent(String recipient,
                                    String channel,
                                    String eventType,
                                    boolean success) {

        String timestamp = LocalDateTime.now().format(formatter);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"recipient\":\"%s\",\"channel\":\"%s\",\"eventType\":\"%s\",\"success\":%s}%n",
                timestamp, recipient, channel, eventType, success
        );

        try {
            Files.writeString(
                    logFilePath,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.error("Failed to write notification audit log (non-fatal). Reason: {}", e.getMessage());
        }
    }
}
