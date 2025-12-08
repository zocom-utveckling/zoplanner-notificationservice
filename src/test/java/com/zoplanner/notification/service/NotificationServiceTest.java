package com.zoplanner.notification.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.zoplanner.notification.model.Notification;
import com.zoplanner.notification.repository.NotificationRepository;
import com.zoplanner.notification.dto.NotificationDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void testMarkAsRead() {
        NotificationRepository repository = mock(NotificationRepository.class); // Skapa mock repository
        NotificationService service = new NotificationService(repository);


        Notification notification = new Notification();  // Skapa en notifikation
        notification.setId(1L);
        notification.setRead(false);

        when(repository.findById(1L)).thenReturn(Optional.of(notification));  // Säg åt mock att returnera notifikationen


        NotificationDTO notificationDTO = service.markAsRead(1L);
        //service.markAsRead(1L); // Anropa metoden


        //assertTrue(notification.isRead());
        assertTrue(notification.isRead());
        verify(repository).save(notification);

        assertEquals(1L, notificationDTO.getId());
        assertTrue(notificationDTO.isRead());
    }

    @Test
    void testGetNotificationsByUserId() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationService service = new NotificationService(repository);

        Notification notif1 = new Notification(); // Skapa test-notifikationer
        notif1.setUserId(100L);

        Notification notif2 = new Notification();
        notif2.setUserId(100L);
        when(repository.findByUserId(100L)).thenReturn(Arrays.asList(notif1, notif2));  // Säg åt mock att returnera listan


        //service.getNotificationsByUserId(100L);
        List<NotificationDTO> result = service.getNotificationsByUserId(100L); // Anropa metoden

        //verify(repository).findByUserId(100L);
        //assertEquals(2, result.size());
        verify(repository).findByUserId(100L);
        assertEquals(2, result.size());
    }

    @Test
    void testGetUnreadNotifications() {
        NotificationRepository repository = mock(NotificationRepository.class);
        NotificationService service = new NotificationService(repository);

        Notification unreadNotif = new Notification();  // Skapa en oläst notifikation
        unreadNotif.setUserId(100L);
        unreadNotif.setRead(false);

        when(repository.findByUserIdAndIsRead(100L, false)).thenReturn(Arrays.asList(unreadNotif));  // Säg åt mock att returnera den

        //service.getUnreadNotifications(100L);
        List<NotificationDTO> result = service.getUnreadNotifications(100L);  // Anropa metoden

        verify(repository).findByUserIdAndIsRead(100L, false);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

}
