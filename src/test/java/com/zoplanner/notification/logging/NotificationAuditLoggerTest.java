package com.zoplanner.notification.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

public class NotificationAuditLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void testLogNotificationWritesToFile() throws Exception {

        // Arrange
        Path logFile = tempDir.resolve("notification.log");
        NotificationAuditLogger logger = new NotificationAuditLogger(logFile.toString());

        // Act
        logger.logNotificationSent("test@test.com", "EMAIL", "new-assignment", true);

        // Assert, att filen finns
        assertThat(Files.exists(logFile)).isTrue();

        // Assert att det är Json format
        String content = Files.readString(logFile);
        assertThat(content).contains("\"recipient\":\"test@test.com\"");
        assertThat(content).contains("\"channel\":\"EMAIL\"");
        assertThat(content).contains("\"eventType\":\"new-assignment\"");
        assertThat(content).contains("\"success\":true");

    }

}
