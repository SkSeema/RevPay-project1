package org.example.service;

import org.example.dao.NotificationDAO;
import org.example.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationService notificationService;

    private List<Notification> testNotifications;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationDAO);
        testNotifications = new ArrayList<>();
        Notification notification1 = new Notification();
        notification1.setNotificationId(1);
        notification1.setUserId(1);
        notification1.setType("TRANSACTION");
        notification1.setMessage("Money Sent: ₹500");
        notification1.setRead(false);
        testNotifications.add(notification1);
    }

    @Test
    void testCreateNotification() {
        // Arrange
        when(notificationDAO.createNotification(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.createNotification(1, "TRANSACTION", "Test message");

        // Assert
        verify(notificationDAO, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testNotifyTransaction() {
        // Arrange
        when(notificationDAO.createNotification(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.notifyTransaction(1, "Deposit", new BigDecimal("1000"), "Added to wallet");

        // Assert
        verify(notificationDAO, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testNotifyMoneyRequest() {
        // Arrange
        when(notificationDAO.createNotification(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.notifyMoneyRequest(1, "Money Request", new BigDecimal("500"), "John Doe");

        // Assert
        verify(notificationDAO, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testNotifyCardChange() {
        // Arrange
        when(notificationDAO.createNotification(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.notifyCardChange(1, "Added", "1234");

        // Assert
        verify(notificationDAO, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testNotifyLowBalance() {
        // Arrange
        when(notificationDAO.createNotification(any(Notification.class))).thenReturn(1);

        // Act
        notificationService.notifyLowBalance(1, new BigDecimal("450"));

        // Assert
        verify(notificationDAO, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testGetUnreadNotifications() {
        // Arrange
        when(notificationDAO.getUnreadNotifications(1)).thenReturn(testNotifications);

        // Act
        List<Notification> result = notificationService.getUnreadNotifications(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationDAO, times(1)).getUnreadNotifications(1);
    }

    @Test
    void testGetUnreadCount() {
        // Arrange
        when(notificationDAO.getUnreadCount(1)).thenReturn(5);

        // Act
        int count = notificationService.getUnreadCount(1);

        // Assert
        assertEquals(5, count);
        verify(notificationDAO, times(1)).getUnreadCount(1);
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        when(notificationDAO.markAsRead(1)).thenReturn(true);

        // Act
        boolean result = notificationService.markAsRead(1);

        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).markAsRead(1);
    }

    @Test
    void testMarkAllAsRead() {
        // Arrange
        when(notificationDAO.markAllAsRead(1)).thenReturn(true);

        // Act
        boolean result = notificationService.markAllAsRead(1);

        // Assert
        assertTrue(result);
        verify(notificationDAO, times(1)).markAllAsRead(1);
    }

    @Test
    void testGetAllNotifications() {
        // Arrange
        when(notificationDAO.getAllNotifications(1)).thenReturn(testNotifications);

        // Act
        List<Notification> result = notificationService.getAllNotifications(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationDAO, times(1)).getAllNotifications(1);
    }
}
