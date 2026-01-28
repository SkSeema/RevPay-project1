package org.example.controller;

import org.example.model.Notification;
import org.example.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class NotificationController {

    private static final Logger logger = LogManager.getLogger(NotificationController.class);
    private final NotificationService notificationService = new NotificationService();
    private final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");


    public void viewNotifications(int userId) {
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
        
        if (unreadNotifications.isEmpty()) {
            logger.info("\n📬 No new notifications");
            return;
        }

        logger.info("\n========== NOTIFICATIONS ==========");
        logger.info("You have {} unread notification(s)\n", unreadNotifications.size());

        int count = 1;
        for (Notification notification : unreadNotifications) {
            displayNotification(notification, count++);
        }

        logger.info("===================================");
        
        // Mark all as read after displaying
        notificationService.markAllAsRead(userId);
        logger.info("\n✓ All notifications marked as read");
    }


    private void displayNotification(Notification notification, int index) {
        String icon = switch (notification.getType()) {
            case "TRANSACTION" -> "💳";
            case "REQUEST" -> "💰";
            case "CARD" -> "🔔";
            case "ALERT" -> "⚠️";
            default -> "📢";
        };

        logger.info("{}. {} {} [{}]", 
            index,
            icon,
            notification.getMessage(),
            notification.getCreatedAt().format(formatter));
    }


    public int getUnreadCount(int userId) {
        return notificationService.getUnreadCount(userId);
    }
}
