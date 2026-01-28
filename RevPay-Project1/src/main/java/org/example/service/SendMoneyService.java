package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.Transaction;
import org.example.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SendMoneyService {

    private static final Logger logger = LogManager.getLogger(SendMoneyService.class);
    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;
    private final NotificationService notificationService;
    private final UserDAO userDAO;
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500");

    // Default constructor for production use
    public SendMoneyService() {
        this.walletDAO = new WalletDAO();
        this.transactionDAO = new TransactionDAO();
        this.notificationService = new NotificationService();
        this.userDAO = new UserDAO();
    }

    // Constructor for testing with dependency injection
    public SendMoneyService(WalletDAO walletDAO, TransactionDAO transactionDAO, NotificationService notificationService, UserDAO userDAO) {
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
        this.notificationService = notificationService;
        this.userDAO = userDAO;
    }

    // Send money logic
    public boolean sendMoney(int senderUserId, String recipientIdentifier, BigDecimal amount, String note) {
        try {
            int recipientUserId = walletDAO.getUserIdByIdentifier(recipientIdentifier);
            if (recipientUserId == -1) {
                logger.warn("Recipient not found: {}", recipientIdentifier);
                return false;
            }

            var senderWallet = walletDAO.getWalletByUserId(senderUserId);
            var recipientWallet = walletDAO.getWalletByUserId(recipientUserId);

            if (senderWallet == null || recipientWallet == null) {
                logger.warn("Wallet missing for sender or recipient");
                return false;
            }

            if (senderWallet.getBalance().compareTo(amount) < 0) {
                logger.warn("Insufficient balance");
                return false;
            }

            // Use transfer method for atomic transaction
            boolean success = walletDAO.transfer(senderUserId, recipientUserId, amount);
            
            // Log transaction
            if (success) {
                Transaction transaction = new Transaction();
                transaction.setFromWalletId(senderWallet.getWalletId());
                transaction.setToWalletId(recipientWallet.getWalletId());
                transaction.setAmount(amount);
                transaction.setTransactionType("SEND");
                transaction.setStatus("SUCCESS");
                transaction.setNote(note != null ? note : "Money sent");
                transaction.setCreatedAt(LocalDateTime.now());
                transactionDAO.logTransaction(transaction);
                
                logger.info("Transfer note: {}", note);
                
                // Get user names for notifications
                User sender = userDAO.getUserById(senderUserId);
                User recipient = userDAO.getUserById(recipientUserId);
                
                // Notify sender
                notificationService.notifyTransaction(senderUserId, "Money Sent", amount, "to " + recipient.getFullName());
                
                // Notify recipient
                notificationService.notifyTransaction(recipientUserId, "Money Received", amount, "from " + sender.getFullName());
                
                // Check sender's balance for low balance alert
                var updatedSenderWallet = walletDAO.getWalletByUserId(senderUserId);
                if (updatedSenderWallet.getBalance().compareTo(LOW_BALANCE_THRESHOLD) < 0) {
                    notificationService.notifyLowBalance(senderUserId, updatedSenderWallet.getBalance());
                }
            }
            
            return success;

        } catch (Exception e) {
            logger.error("Send money failed", e);
            return false;
        }
    }
}
