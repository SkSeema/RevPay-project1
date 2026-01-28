package org.example.service;

import org.example.dao.NotificationDAO;
import org.example.model.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;

public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);
    private final NotificationDAO notificationDAO;

    // Default constructor for production use
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    // Constructor for testing with dependency injection
    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    // Create notification
    public void createNotification(int userId, String type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        
        int notificationId = notificationDAO.createNotification(notification);
        if (notificationId > 0) {
            logger.debug("Notification created: userId={}, type={}", userId, type);
        }
    }

    // Transaction notification
    public void notifyTransaction(int userId, String transactionType, BigDecimal amount, String details) {
        String message = String.format("%s: ₹%s - %s", transactionType, amount, details);
        createNotification(userId, "TRANSACTION", message);
    }

    // Money request notification
    public void notifyMoneyRequest(int userId, String requestType, BigDecimal amount, String from) {
        String message = String.format("%s: ₹%s from %s", requestType, amount, from);
        createNotification(userId, "REQUEST", message);
    }

    // Card change notification
    public void notifyCardChange(int userId, String action, String cardLast4) {
        String message = String.format("Card %s: ****%s", action, cardLast4);
        createNotification(userId, "CARD", message);
    }

    // Low balance alert
    public void notifyLowBalance(int userId, BigDecimal currentBalance) {
        String message = String.format("Low Balance Alert: Your balance is ₹%s", currentBalance);
        createNotification(userId, "ALERT", message);
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDAO.getUnreadNotifications(userId);
    }

    // Get unread count
    public int getUnreadCount(int userId) {
        return notificationDAO.getUnreadCount(userId);
    }

    // Mark as read
    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    // Mark all as read
    public boolean markAllAsRead(int userId) {
        return notificationDAO.markAllAsRead(userId);
    }

    // Get all notifications
    public List<Notification> getAllNotifications(int userId) {
        return notificationDAO.getAllNotifications(userId);
    }
}
