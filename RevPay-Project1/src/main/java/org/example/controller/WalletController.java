package org.example.controller;

import org.example.model.Wallet;
import org.example.service.NotificationService;
import org.example.service.TransactionService;
import org.example.service.WalletService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Scanner;

public class WalletController {

    private static final Logger logger = LogManager.getLogger(WalletController.class);
    private final WalletService walletService = new WalletService();
    private final TransactionService transactionService = new TransactionService();
    private final NotificationService notificationService = new NotificationService();
    private final Scanner sc = new Scanner(System.in);
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500");

    public void showWallet(int userId) {
        Wallet wallet = walletService.getWallet(userId);
        if (wallet != null) {
            logger.info("Wallet Balance: {} {}", wallet.getBalance(), wallet.getCurrency());
        } else {
            logger.warn("No wallet found for userId={}", userId);
        }
    }

    public void deposit(int userId) {
        logger.info("Enter amount to deposit: ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        if (walletService.deposit(userId, amount)) {
            Wallet wallet = walletService.getWallet(userId);
            logger.info("Deposit successful. New balance: {}", wallet.getBalance());
            

            notificationService.notifyTransaction(userId, "Deposit", amount, "Added to wallet");
        } else {
            logger.warn("Deposit failed.");
        }
    }

    public void withdraw(int userId) {
        logger.info("Enter amount to withdraw: ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        if (walletService.withdraw(userId, amount)) {
            Wallet wallet = walletService.getWallet(userId);
            logger.info("Withdrawal successful. New balance: {}", wallet.getBalance());
            

            notificationService.notifyTransaction(userId, "Withdrawal", amount, "Withdrawn from wallet");
            

            if (wallet.getBalance().compareTo(LOW_BALANCE_THRESHOLD) < 0) {
                notificationService.notifyLowBalance(userId, wallet.getBalance());
            }
        } else {
            logger.warn("Withdrawal failed. Check balance.");
        }
    }
    
    public void viewTransactionHistory(int userId) {
        transactionService.displayTransactionHistory(userId);
    }
    
    public void viewTransactionHistoryWithFilters(int userId) {
        logger.info("\n===== TRANSACTION HISTORY FILTERS =====");
        logger.info("Apply filters? (yes/no):");
        String applyFilters = sc.nextLine().toLowerCase();
        
        if (!applyFilters.equals("yes") && !applyFilters.equals("y")) {
            transactionService.displayTransactionHistory(userId);
            return;
        }
        

        logger.info("\nFilter by type? (1-SENT, 2-RECEIVED, 3-DEPOSIT, 4-WITHDRAWAL, 0-All):");
        String typeChoice = sc.nextLine();
        String typeFilter = null;
        switch (typeChoice) {
            case "1" -> typeFilter = "SENT";
            case "2" -> typeFilter = "RECEIVED";
            case "3" -> typeFilter = "DEPOSIT";
            case "4" -> typeFilter = "WITHDRAWAL";
        }
        

        logger.info("\nFilter by date range? (yes/no):");
        String dateRangeChoice = sc.nextLine().toLowerCase();
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        
        if (dateRangeChoice.equals("yes") || dateRangeChoice.equals("y")) {
            try {
                logger.info("Enter start date (YYYY-MM-DD) or press Enter to skip:");
                String startDateStr = sc.nextLine();
                if (!startDateStr.isEmpty()) {
                    startDate = java.time.LocalDate.parse(startDateStr).atStartOfDay();
                }
                
                logger.info("Enter end date (YYYY-MM-DD) or press Enter to skip:");
                String endDateStr = sc.nextLine();
                if (!endDateStr.isEmpty()) {
                    endDate = java.time.LocalDate.parse(endDateStr).atTime(23, 59, 59);
                }
            } catch (Exception e) {
                logger.warn("Invalid date format. Skipping date filter.");
            }
        }
        

        logger.info("\nFilter by amount range? (yes/no):");
        String amountRangeChoice = sc.nextLine().toLowerCase();
        BigDecimal minAmount = null;
        BigDecimal maxAmount = null;
        
        if (amountRangeChoice.equals("yes") || amountRangeChoice.equals("y")) {
            try {
                logger.info("Enter minimum amount or press Enter to skip:");
                String minStr = sc.nextLine();
                if (!minStr.isEmpty()) {
                    minAmount = new BigDecimal(minStr);
                }
                
                logger.info("Enter maximum amount or press Enter to skip:");
                String maxStr = sc.nextLine();
                if (!maxStr.isEmpty()) {
                    maxAmount = new BigDecimal(maxStr);
                }
            } catch (Exception e) {
                logger.warn("Invalid amount format. Skipping amount filter.");
            }
        }
        
        // Status filter
        logger.info("\nFilter by status? (1-SUCCESS, 2-FAILED, 3-PENDING, 0-All):");
        String statusChoice = sc.nextLine();
        String statusFilter = null;
        switch (statusChoice) {
            case "1" -> statusFilter = "SUCCESS";
            case "2" -> statusFilter = "FAILED";
            case "3" -> statusFilter = "PENDING";
        }
        
        // Search term
        logger.info("\nSearch in notes/counterparty? (Enter text or press Enter to skip):");
        String searchTerm = sc.nextLine();
        if (searchTerm.isEmpty()) {
            searchTerm = null;
        }
        
        // Display filtered results
        transactionService.displayTransactionHistory(userId, typeFilter, startDate, endDate, 
                                                    minAmount, maxAmount, statusFilter, searchTerm);
    }
    
    public WalletService getWalletService() {
        return this.walletService;
    }

}
