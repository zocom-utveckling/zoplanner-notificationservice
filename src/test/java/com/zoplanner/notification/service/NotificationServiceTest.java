package com.zoplanner.notification.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Test
    void testCreateNotification() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);

        NotificationService service = new NotificationService(repository);

        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");

        service.createNotification(dto);
        verify(repository, times(1)).save(any(Notification.class));
    }

}
