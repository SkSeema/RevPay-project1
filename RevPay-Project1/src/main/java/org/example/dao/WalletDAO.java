package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.Wallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;

public class WalletDAO {

    private static final Logger logger = LogManager.getLogger(WalletDAO.class);

    // Create wallet for new user
    public boolean createWallet(int userId) {
        String sql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, BigDecimal.ZERO);  // Initial balance
            int rows = ps.executeUpdate();
            logger.info("Wallet created for userId={}, rows={}", userId, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error creating wallet for userId={}", userId, e);
            throw new RuntimeException(e);
        }
    }

    // Fetch wallet by userId
    public Wallet getWalletByUserId(int userId) {
        String sql = "SELECT * FROM wallets WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setWalletId(rs.getInt("wallet_id"));
                wallet.setUserId(rs.getInt("user_id"));
                wallet.setBalance(rs.getBigDecimal("balance"));
                wallet.setCurrency(rs.getString("currency"));
                wallet.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                return wallet;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error fetching wallet for userId={}", userId, e);
            throw new RuntimeException(e);
        }
    }

    // Fetch wallet by walletId
    public Wallet getWalletById(int walletId) {
        String sql = "SELECT * FROM wallets WHERE wallet_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, walletId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setWalletId(rs.getInt("wallet_id"));
                wallet.setUserId(rs.getInt("user_id"));
                wallet.setBalance(rs.getBigDecimal("balance"));
                wallet.setCurrency(rs.getString("currency"));
                wallet.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                return wallet;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error fetching wallet for walletId={}", walletId, e);
            throw new RuntimeException(e);
        }
    }

    // Update wallet balance
    public boolean updateBalance(int userId, BigDecimal newBalance) {
        String sql = "UPDATE wallets SET balance = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            logger.info("Wallet updated for userId={}, newBalance={}, rows={}", userId, newBalance, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating wallet for userId={}", userId, e);
            throw new RuntimeException(e);
        }
    }
    // Fetch userId by username/email/phone
    public int getUserIdByIdentifier(String identifier) {
        String sql = "SELECT user_id FROM users WHERE email = ? OR phone = ? OR full_name = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching userId for identifier={}", identifier, e);
        }
        return -1; // recipient not found
    }

    // Transfer money between wallets
    public boolean transfer(int fromUserId, int toUserId, BigDecimal amount) {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance();
            conn.setAutoCommit(false);
            
            // Get sender's balance
            String getSql = "SELECT balance FROM wallets WHERE user_id = ?";
            BigDecimal fromBalance = null;
            try (PreparedStatement ps = conn.prepareStatement(getSql)) {
                ps.setInt(1, fromUserId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    fromBalance = rs.getBigDecimal("balance");
                }
            }
            
            if (fromBalance == null || fromBalance.compareTo(amount) < 0) {
                logger.error("Insufficient balance or wallet not found for userId={}", fromUserId);
                conn.rollback();
                return false;
            }
            
            // Check receiver exists
            BigDecimal toBalance = null;
            try (PreparedStatement ps = conn.prepareStatement(getSql)) {
                ps.setInt(1, toUserId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    toBalance = rs.getBigDecimal("balance");
                }
            }
            
            if (toBalance == null) {
                logger.error("Receiver wallet not found for userId={}", toUserId);
                conn.rollback();
                return false;
            }
            
            // Update both balances
            String updateSql = "UPDATE wallets SET balance = ?, last_updated = CURRENT_TIMESTAMP WHERE user_id = ?";
            
            // Deduct from sender
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, fromBalance.subtract(amount));
                ps.setInt(2, fromUserId);
                ps.executeUpdate();
            }
            
            // Add to receiver
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, toBalance.add(amount));
                ps.setInt(2, toUserId);
                ps.executeUpdate();
            }
            
            conn.commit();
            logger.info("Transfer successful: {} from userId={} to userId={}", amount, fromUserId, toUserId);
            return true;
            
        } catch (SQLException e) {
            logger.error("Transfer failed", e);
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
                    // Don't close the connection as it's managed by DBConnection singleton
                } catch (SQLException e) {
                    logger.error("Failed to reset autocommit", e);
                }
            }
        }
    }

}

