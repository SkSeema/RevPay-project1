package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.PaymentMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDAO {

    private static final Logger logger = LogManager.getLogger(PaymentMethodDAO.class);

    // Add payment method
    public boolean addPaymentMethod(PaymentMethod paymentMethod) {
        String sql = "INSERT INTO payment_methods (user_id, method_type, encrypted_details, is_default) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentMethod.getUserId());
            ps.setString(2, paymentMethod.getMethodType());
            ps.setString(3, paymentMethod.getEncryptedDetails());
            ps.setBoolean(4, paymentMethod.isDefault());
            int rows = ps.executeUpdate();
            logger.info("Payment method added for userId={}, rows={}", paymentMethod.getUserId(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error adding payment method for userId={}", paymentMethod.getUserId(), e);
            return false;
        }
    }

    // Get all payment methods for a user
    public List<PaymentMethod> getPaymentMethodsByUserId(int userId) {
        String sql = "SELECT * FROM payment_methods WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        List<PaymentMethod> methods = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PaymentMethod pm = new PaymentMethod();
                pm.setPaymentId(rs.getInt("payment_id"));
                pm.setUserId(rs.getInt("user_id"));
                pm.setMethodType(rs.getString("method_type"));
                pm.setEncryptedDetails(rs.getString("encrypted_details"));
                pm.setDefault(rs.getBoolean("is_default"));
                pm.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                methods.add(pm);
            }
            logger.info("Retrieved {} payment methods for userId={}", methods.size(), userId);
        } catch (SQLException e) {
            logger.error("Error fetching payment methods for userId={}", userId, e);
        }
        return methods;
    }

    // Get payment method by ID
    public PaymentMethod getPaymentMethodById(int paymentId) {
        String sql = "SELECT * FROM payment_methods WHERE payment_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PaymentMethod pm = new PaymentMethod();
                pm.setPaymentId(rs.getInt("payment_id"));
                pm.setUserId(rs.getInt("user_id"));
                pm.setMethodType(rs.getString("method_type"));
                pm.setEncryptedDetails(rs.getString("encrypted_details"));
                pm.setDefault(rs.getBoolean("is_default"));
                pm.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return pm;
            }
        } catch (SQLException e) {
            logger.error("Error fetching payment method by ID={}", paymentId, e);
        }
        return null;
    }

    // Set default payment method
    public boolean setDefaultPaymentMethod(int userId, int paymentId) {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance();
            conn.setAutoCommit(false);

            // First, unset all defaults for this user
            String unsetSql = "UPDATE payment_methods SET is_default = 0 WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(unsetSql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Then set the selected one as default
            String setSql = "UPDATE payment_methods SET is_default = 1 WHERE payment_id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(setSql)) {
                ps.setInt(1, paymentId);
                ps.setInt(2, userId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    logger.info("Default payment method set: paymentId={} for userId={}", paymentId, userId);
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            logger.error("Error setting default payment method", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Rollback failed", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset autocommit", e);
                }
            }
        }
    }

    // Delete payment method
    public boolean deletePaymentMethod(int paymentId, int userId) {
        String sql = "DELETE FROM payment_methods WHERE payment_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            logger.info("Payment method deleted: paymentId={}, rows={}", paymentId, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting payment method paymentId={}", paymentId, e);
            return false;
        }
    }

    // Get default payment method
    public PaymentMethod getDefaultPaymentMethod(int userId) {
        String sql = "SELECT * FROM payment_methods WHERE user_id = ? AND is_default = 1 LIMIT 1";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PaymentMethod pm = new PaymentMethod();
                pm.setPaymentId(rs.getInt("payment_id"));
                pm.setUserId(rs.getInt("user_id"));
                pm.setMethodType(rs.getString("method_type"));
                pm.setEncryptedDetails(rs.getString("encrypted_details"));
                pm.setDefault(rs.getBoolean("is_default"));
                pm.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return pm;
            }
        } catch (SQLException e) {
            logger.error("Error fetching default payment method for userId={}", userId, e);
        }
        return null;
    }
}
