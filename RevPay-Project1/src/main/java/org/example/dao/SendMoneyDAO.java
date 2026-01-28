package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.SendMoney;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SendMoneyDAO {

    private static final Logger logger = LogManager.getLogger(SendMoneyDAO.class);

    // Insert a new send money transaction
    public boolean createTransaction(SendMoney transaction) {
        String sql = "INSERT INTO send_money (sender_user_id, receiver_user_id, amount, timestamp, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, transaction.getSenderUserId());
            ps.setInt(2, transaction.getReceiverUserId());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(transaction.getTimestamp()));
            ps.setString(5, transaction.getStatus());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    transaction.setTransactionId(rs.getInt(1));
                }
                logger.info("Transaction created successfully: sender={}, receiver={}, amount={}",
                        transaction.getSenderUserId(),
                        transaction.getReceiverUserId(),
                        transaction.getAmount());
                return true;
            }
            logger.warn("Transaction creation failed: sender={}, receiver={}, amount={}",
                    transaction.getSenderUserId(),
                    transaction.getReceiverUserId(),
                    transaction.getAmount());
            return false;

        } catch (SQLException e) {
            logger.error("Error creating transaction: sender={}, receiver={}, amount={}",
                    transaction.getSenderUserId(),
                    transaction.getReceiverUserId(),
                    transaction.getAmount(), e);
            throw new RuntimeException(e);
        }
    }

    // Optional: Fetch transaction by ID
    public SendMoney getTransactionById(int transactionId) {
        String sql = "SELECT * FROM send_money WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                SendMoney transaction = new SendMoney();
                transaction.setTransactionId(rs.getInt("transaction_id"));
                transaction.setSenderUserId(rs.getInt("sender_user_id"));
                transaction.setReceiverUserId(rs.getInt("receiver_user_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                transaction.setStatus(rs.getString("status"));
                return transaction;
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error fetching transactionId={}", transactionId, e);
            throw new RuntimeException(e);
        }
    }
}
