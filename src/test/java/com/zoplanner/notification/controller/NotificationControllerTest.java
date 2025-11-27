package com.zoplanner.notification.controller;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.zoplanner.notification.service.NotificationService;
import com.zoplanner.notification.dto.NotificationDTO;

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
}
