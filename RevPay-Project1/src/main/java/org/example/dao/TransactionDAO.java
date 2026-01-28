package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final Logger logger = LogManager.getLogger(TransactionDAO.class);

    // Log a transaction
    public boolean logTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (from_wallet_id, to_wallet_id, amount, transaction_type, status, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (transaction.getFromWalletId() > 0) {
                ps.setInt(1, transaction.getFromWalletId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            
            if (transaction.getToWalletId() > 0) {
                ps.setInt(2, transaction.getToWalletId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getTransactionType());
            ps.setString(5, transaction.getStatus() != null ? transaction.getStatus() : "SUCCESS");
            ps.setString(6, transaction.getNote());
            ps.setTimestamp(7, Timestamp.valueOf(transaction.getCreatedAt() != null ? transaction.getCreatedAt() : java.time.LocalDateTime.now()));
            
            int rows = ps.executeUpdate();
            logger.info("Transaction logged: type={}, amount={}, rows={}", transaction.getTransactionType(), transaction.getAmount(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error logging transaction", e);
            return false;
        }
    }

    // Get transaction history by wallet ID
    public List<Transaction> getTransactionsByWalletId(int walletId) {
        String sql = "SELECT * FROM transactions WHERE from_wallet_id = ? OR to_wallet_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Transaction> list = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, walletId);
            ps.setInt(2, walletId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setFromWalletId(rs.getInt("from_wallet_id"));
                t.setToWalletId(rs.getInt("to_wallet_id"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setTransactionType(rs.getString("transaction_type"));
                t.setStatus(rs.getString("status"));
                t.setNote(rs.getString("note"));
                t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(t);
            }
            
            logger.info("Retrieved {} transactions for walletId={}", list.size(), walletId);
        } catch (SQLException e) {
            logger.error("Error fetching transactions for walletId={}", walletId, e);
        }
        
        return list;
    }

    // Get transaction history by user ID
    public List<Transaction> getTransactionsByUserId(int userId) {
        String sql = "SELECT t.* FROM transactions t " +
                     "JOIN wallets w ON (t.from_wallet_id = w.wallet_id OR t.to_wallet_id = w.wallet_id) " +
                     "WHERE w.user_id = ? ORDER BY t.created_at DESC LIMIT 50";
        List<Transaction> list = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setFromWalletId(rs.getInt("from_wallet_id"));
                t.setToWalletId(rs.getInt("to_wallet_id"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setTransactionType(rs.getString("transaction_type"));
                t.setStatus(rs.getString("status"));
                t.setNote(rs.getString("note"));
                t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(t);
            }
            
            logger.info("Retrieved {} transactions for userId={}", list.size(), userId);
        } catch (SQLException e) {
            logger.error("Error fetching transactions for userId={}", userId, e);
        }
        
        return list;
    }
}
