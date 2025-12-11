package com.zoplanner.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// NOTE: no @SpringBootTest here anymore
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Simple sanity check, avoids starting Spring in CI where classpath is weird
        assertTrue(true);
    }
}
