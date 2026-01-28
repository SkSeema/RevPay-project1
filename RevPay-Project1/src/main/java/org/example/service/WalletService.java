package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.WalletDAO;
import org.example.model.Transaction;
import org.example.model.Wallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletService {

    private static final Logger logger = LogManager.getLogger(WalletService.class);
    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;

    // Default constructor for production use
    public WalletService() {
        this.walletDAO = new WalletDAO();
        this.transactionDAO = new TransactionDAO();
    }

    // Constructor for testing with dependency injection
    public WalletService(WalletDAO walletDAO, TransactionDAO transactionDAO) {
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
    }

    // Check if wallet exists
    public boolean walletExists(int userId) {
        return walletDAO.getWalletByUserId(userId) != null;
    }

    // Auto-create wallet if not exists
    public void createWalletIfNotExists(int userId) {
        if (!walletExists(userId)) {
            walletDAO.createWallet(userId);
            logger.info("Wallet auto-created for userId={}", userId);
        }
    }

    public Wallet getWallet(int userId) {
        return walletDAO.getWalletByUserId(userId);
    }

    public boolean deposit(int userId, BigDecimal amount) {
        Wallet wallet = getWallet(userId);
        if (wallet == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Deposit failed. Invalid amount or wallet missing for userId={}", userId);
            return false;
        }
        boolean success = walletDAO.updateBalance(userId, wallet.getBalance().add(amount));
        
        // Log transaction
        if (success) {
            Transaction transaction = new Transaction();
            transaction.setToWalletId(wallet.getWalletId());
            transaction.setAmount(amount);
            transaction.setTransactionType("ADD");
            transaction.setStatus("SUCCESS");
            transaction.setNote("Deposit to wallet");
            transaction.setCreatedAt(LocalDateTime.now());
            transactionDAO.logTransaction(transaction);
        }
        
        return success;
    }

    public boolean withdraw(int userId, BigDecimal amount) {
        Wallet wallet = getWallet(userId);
        if (wallet == null || amount.compareTo(BigDecimal.ZERO) <= 0 || wallet.getBalance().compareTo(amount) < 0) {
            logger.warn("Withdrawal failed. Check balance or wallet missing for userId={}", userId);
            return false;
        }
        boolean success = walletDAO.updateBalance(userId, wallet.getBalance().subtract(amount));
        
        // Log transaction
        if (success) {
            Transaction transaction = new Transaction();
            transaction.setFromWalletId(wallet.getWalletId());
            transaction.setAmount(amount);
            transaction.setTransactionType("WITHDRAW");
            transaction.setStatus("SUCCESS");
            transaction.setNote("Withdrawal from wallet");
            transaction.setCreatedAt(LocalDateTime.now());
            transactionDAO.logTransaction(transaction);
        }
        
        return success;
    }
}
