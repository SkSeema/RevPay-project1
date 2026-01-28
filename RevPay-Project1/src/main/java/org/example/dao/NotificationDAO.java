package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private static final Logger logger = LogManager.getLogger(NotificationDAO.class);

    // Create notification
    public int createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, message, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getType());
            ps.setString(3, notification.getMessage());
            ps.setBoolean(4, notification.isRead());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error creating notification", e);
            return 0;
        }
    }

    // Get unread notifications for user
    public List<Notification> getUnreadNotifications(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching unread notifications", e);
        }
        return notifications;
    }

    // Get unread notification count
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting unread count", e);
        }
        return 0;
    }

    // Mark notification as read
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notification as read", e);
            return false;
        }
    }

    // Mark all notifications as read for user
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking all notifications as read", e);
            return false;
        }
    }

    // Get all notifications (read and unread) for user
    public List<Notification> getAllNotifications(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> notifications = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all notifications", e);
        }
        return notifications;
    }

    // Helper method to map ResultSet to Notification
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setType(rs.getString("type"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return notification;
    }
}
