package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.model.Wallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {

    private static final Logger logger = LogManager.getLogger(TransactionService.class);
    private final TransactionDAO transactionDAO;
    private final WalletDAO walletDAO;
    private final UserDAO userDAO;

    // Default constructor for production use
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.walletDAO = new WalletDAO();
        this.userDAO = new UserDAO();
    }

    // Constructor for testing with dependency injection
    public TransactionService(TransactionDAO transactionDAO, WalletDAO walletDAO, UserDAO userDAO) {
        this.transactionDAO = transactionDAO;
        this.walletDAO = walletDAO;
        this.userDAO = userDAO;
    }

    // Display transaction history for a user with optional filters
    public void displayTransactionHistory(int userId, String typeFilter, LocalDateTime startDate, 
                                         LocalDateTime endDate, BigDecimal minAmount, 
                                         BigDecimal maxAmount, String statusFilter, String searchTerm) {
        logger.info("Fetching transaction history for userId={}", userId);
        
        // Get user's wallet
        Wallet wallet = walletDAO.getWalletByUserId(userId);
        if (wallet == null) {
            logger.warn("No wallet found for userId={}", userId);
            return;
        }

        List<Transaction> transactions = transactionDAO.getTransactionsByWalletId(wallet.getWalletId());
        
        // Apply filters
        transactions = filterTransactions(transactions, wallet.getWalletId(), typeFilter, 
                                         startDate, endDate, minAmount, maxAmount, statusFilter, searchTerm);
        
        if (transactions.isEmpty()) {
            logger.info("No transactions found matching the filters.");
            return;
        }

        logger.info("================ Transaction History ================");
        logger.info("Total Transactions: {}", transactions.size());
        if (typeFilter != null) logger.info("Filter - Type: {}", typeFilter);
        if (startDate != null || endDate != null) {
            logger.info("Filter - Date Range: {} to {}", 
                startDate != null ? startDate.toLocalDate() : "Beginning", 
                endDate != null ? endDate.toLocalDate() : "Now");
        }
        if (minAmount != null || maxAmount != null) {
            logger.info("Filter - Amount Range: ₹{} to ₹{}", 
                minAmount != null ? minAmount : "0", 
                maxAmount != null ? maxAmount : "∞");
        }
        if (statusFilter != null) logger.info("Filter - Status: {}", statusFilter);
        if (searchTerm != null) logger.info("Filter - Search: {}", searchTerm);
        logger.info("=====================================================");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Transaction t : transactions) {
            String direction = "";
            String counterparty = "";
            
            // Determine transaction direction and get counterparty info
            if ("ADD".equals(t.getTransactionType())) {
                direction = "💰 DEPOSIT";
                counterparty = "Your Wallet";
            } else if ("WITHDRAW".equals(t.getTransactionType())) {
                direction = "💸 WITHDRAWAL";
                counterparty = "Your Wallet";
            } else if (t.getFromWalletId() == wallet.getWalletId()) {
                // Money sent
                direction = "📤 SENT";
                counterparty = getUserInfoByWalletId(t.getToWalletId());
            } else if (t.getToWalletId() == wallet.getWalletId()) {
                // Money received
                direction = "📥 RECEIVED";
                counterparty = getUserInfoByWalletId(t.getFromWalletId());
            }

            logger.info("-----------------------------------------------------");
            logger.info("ID: {} | {} | ₹{}", t.getTransactionId(), direction, t.getAmount());
            logger.info("Counterparty: {}", counterparty);
            if (t.getNote() != null && !t.getNote().isEmpty()) {
                logger.info("Note: {}", t.getNote());
            }
            logger.info("Status: {} | Date: {}", t.getStatus(), t.getCreatedAt().format(formatter));
        }

        logger.info("=====================================================");
    }

    // Display transaction history without filters (original method)
    public void displayTransactionHistory(int userId) {
        displayTransactionHistory(userId, null, null, null, null, null, null, null);
    }

    // Filter transactions based on criteria
    private List<Transaction> filterTransactions(List<Transaction> transactions, int userWalletId,
                                                String typeFilter, LocalDateTime startDate, 
                                                LocalDateTime endDate, BigDecimal minAmount, 
                                                BigDecimal maxAmount, String statusFilter, String searchTerm) {
        return transactions.stream()
            .filter(t -> {
                // Type filter (SEND, RECEIVE, ADD, WITHDRAW, or direction-based like SENT, RECEIVED)
                if (typeFilter != null) {
                    if (typeFilter.equals("SENT") && t.getFromWalletId() != userWalletId) return false;
                    if (typeFilter.equals("RECEIVED") && t.getToWalletId() != userWalletId) return false;
                    if (typeFilter.equals("DEPOSIT") && !t.getTransactionType().equals("ADD")) return false;
                    if (typeFilter.equals("WITHDRAWAL") && !t.getTransactionType().equals("WITHDRAW")) return false;
                    if (!typeFilter.equals("SENT") && !typeFilter.equals("RECEIVED") && 
                        !typeFilter.equals("DEPOSIT") && !typeFilter.equals("WITHDRAWAL") &&
                        !t.getTransactionType().equals(typeFilter)) return false;
                }
                
                // Date range filter
                if (startDate != null && t.getCreatedAt().isBefore(startDate)) return false;
                if (endDate != null && t.getCreatedAt().isAfter(endDate)) return false;
                
                // Amount range filter
                if (minAmount != null && t.getAmount().compareTo(minAmount) < 0) return false;
                if (maxAmount != null && t.getAmount().compareTo(maxAmount) > 0) return false;
                
                // Status filter
                if (statusFilter != null && !t.getStatus().equalsIgnoreCase(statusFilter)) return false;
                
                // Search term (searches in note and counterparty info)
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                    String counterparty = "";
                    
                    if (t.getFromWalletId() > 0 && t.getFromWalletId() != userWalletId) {
                        counterparty = getUserInfoByWalletId(t.getFromWalletId()).toLowerCase();
                    } else if (t.getToWalletId() > 0 && t.getToWalletId() != userWalletId) {
                        counterparty = getUserInfoByWalletId(t.getToWalletId()).toLowerCase();
                    }
                    
                    String search = searchTerm.toLowerCase();
                    if (!note.contains(search) && !counterparty.contains(search)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }

    // Helper method to get user info by wallet ID
    private String getUserInfoByWalletId(int walletId) {
        Wallet wallet = walletDAO.getWalletById(walletId);
        if (wallet == null) {
            return "Unknown User";
        }
        
        User user = userDAO.getUserById(wallet.getUserId());
        if (user == null) {
            return "Unknown User (WalletID: " + walletId + ")";
        }
        
        return user.getFullName() + " (" + user.getEmail() + ")";
    }

    // Get transactions by user ID (for programmatic use)
    public List<Transaction> getTransactionHistory(int userId) {
        Wallet wallet = walletDAO.getWalletByUserId(userId);
        if (wallet == null) {
            logger.warn("No wallet found for userId={}", userId);
            return List.of();
        }
        return transactionDAO.getTransactionsByWalletId(wallet.getWalletId());
    }
}
