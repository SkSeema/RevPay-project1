package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    // Insert new user
    public int insertUser(User user) {
        String sql = "INSERT INTO users (email, phone, password_hash, transaction_pin_hash, full_name, account_type, security_question, security_answer_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getTransactionPinHash());
            ps.setString(5, user.getFullName());
            ps.setString(6, user.getAccountType() != null ? user.getAccountType() : "PERSONAL");
            ps.setString(7, user.getSecurityQuestion());
            ps.setString(8, user.getSecurityAnswerHash());




            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // return generated userId
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error inserting user", e);
            throw new RuntimeException(e);
        }
    }

    // Fetch user by email or phone
    public User getUserByEmailOrPhone(String input) {
        String sql = "SELECT * FROM users WHERE email = ? OR phone = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setFullName(rs.getString("full_name"));
                u.setAccountType(rs.getString("account_type"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setTransactionPinHash(rs.getString("transaction_pin_hash"));
                u.setSecurityQuestion(rs.getString("security_question"));
                u.setSecurityAnswerHash(rs.getString("security_answer_hash"));
                u.setFailedAttempts(rs.getInt("failed_attempts"));
                u.setAccountLocked(rs.getBoolean("account_locked"));
                return u;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error fetching user", e);
            throw new RuntimeException(e);
        }
    }

    // Fetch user by ID
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setFullName(rs.getString("full_name"));
                u.setAccountType(rs.getString("account_type"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setTransactionPinHash(rs.getString("transaction_pin_hash"));
                u.setSecurityQuestion(rs.getString("security_question"));
                u.setSecurityAnswerHash(rs.getString("security_answer_hash"));
                u.setFailedAttempts(rs.getInt("failed_attempts"));
                u.setAccountLocked(rs.getBoolean("account_locked"));
                return u;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error fetching user by ID", e);
            throw new RuntimeException(e);
        }
    }

    // Update user password
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password", e);
            throw new RuntimeException(e);
        }
    }

    // Increment failed login attempts
    public boolean incrementFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error incrementing failed attempts", e);
            throw new RuntimeException(e);
        }
    }

    // Lock user account
    public boolean lockAccount(int userId) {
        String sql = "UPDATE users SET account_locked = TRUE WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error locking account", e);
            throw new RuntimeException(e);
        }
    }

    // Reset failed attempts (on successful login)
    public boolean resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error resetting failed attempts", e);
            throw new RuntimeException(e);
        }
    }

    // Unlock account and reset failed attempts
    public boolean unlockAccount(int userId) {
        String sql = "UPDATE users SET account_locked = FALSE, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error unlocking account", e);
            throw new RuntimeException(e);
        }
    }

    // Additional methods can be added (update transaction PIN, etc.)
}
