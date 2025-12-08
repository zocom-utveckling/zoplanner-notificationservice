package com.zoplanner.notification.controller;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import com.zoplanner.notification.service.NotificationService;
import com.zoplanner.notification.dto.NotificationDTO;
import java.util.Arrays;

public class NotificationControllerTest {

    @Test
    void testCreateNotification() {

        //Arrange
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);
        NotificationDTO dto = new NotificationDTO();

        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        //Act
        var response = controller.createNotification(dto);

        //Assert
        verify(service, times(1)).createNotification(dto); //Anropar service en gång
        assertThat(response.getStatusCodeValue()).isEqualTo(200); // HTTP OK

    }

    @Test
    void testMarkAsReadEndpoint() {
        // Assert
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);
        NotificationDTO notificationDTO = new NotificationDTO(); // Skapa en läst notifikation

        when(service.markAsRead(1L)).thenReturn(notificationDTO); // Säg åt mock att returnera den

        // Act
        var response = controller.markNotificationAsRead(1L); // Anropa endpoint

        // Assert
        assertEquals(200, response.getStatusCodeValue());  // Kolla att det blev OK
    }

    @Test
    void testGetNotificationsEndpoint() {
        // Arrange
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);

        NotificationDTO notificationDTO = new NotificationDTO();
        when(service.getNotificationsByUserId(100L)).thenReturn(Arrays.asList(notificationDTO));  // Skapa en lista med notifikationer

        // Act
        var response = controller.getNotificationsByUser(100L);  // Anropa endpoint

        // Assert
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetUnreadEndpoint() {
        // Arrange
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);

        NotificationDTO notificationDTO = new NotificationDTO();   // Skapa en notifikation DTO
        when(service.getUnreadNotifications(100L)).thenReturn(Arrays.asList(notificationDTO));

        // Act
        var response = controller.getUnreadNotifications(100L);  // Anropa endpoint

        // Assert
        assertEquals(200, response.getStatusCodeValue());
    }
}
