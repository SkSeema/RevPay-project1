package org.example.service;

import org.example.dao.InvoiceDAO;
import org.example.dao.TransactionDAO;
import org.example.dao.WalletDAO;
import org.example.model.Invoice;
import org.example.model.Transaction;
import org.example.model.Wallet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BusinessAnalyticsService {

    private static final Logger logger = LogManager.getLogger(BusinessAnalyticsService.class);
    private final TransactionDAO transactionDAO;
    private final InvoiceDAO invoiceDAO;
    private final WalletDAO walletDAO;

    // Default constructor for production use
    public BusinessAnalyticsService() {
        this.transactionDAO = new TransactionDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.walletDAO = new WalletDAO();
    }

    // Constructor for testing with dependency injection
    public BusinessAnalyticsService(TransactionDAO transactionDAO, InvoiceDAO invoiceDAO, WalletDAO walletDAO) {
        this.transactionDAO = transactionDAO;
        this.invoiceDAO = invoiceDAO;
        this.walletDAO = walletDAO;
    }

    // Display business analytics dashboard
    public void displayBusinessAnalytics(int userId) {
        logger.info("\n================ BUSINESS ANALYTICS ================");
        
        Wallet wallet = walletDAO.getWalletByUserId(userId);
        if (wallet == null) {
            logger.warn("No wallet found for userId={}", userId);
            return;
        }

        // Transaction Summary
        displayTransactionSummary(wallet.getWalletId());
        
        // Revenue Report
        displayRevenueReport(wallet.getWalletId());
        
        // Invoice Summary
        displayInvoiceSummary(userId);
        
        // Payment Trends
        displayPaymentTrends(wallet.getWalletId());
        
        // Top Customers
        displayTopCustomers(userId);
        
        logger.info("====================================================");
    }

    private void displayTransactionSummary(int walletId) {
        List<Transaction> transactions = transactionDAO.getTransactionsByWalletId(walletId);
        
        long totalTransactions = transactions.size();
        long successfulTransactions = transactions.stream()
            .filter(t -> "SUCCESS".equals(t.getStatus()))
            .count();
        
        BigDecimal totalReceived = transactions.stream()
            .filter(t -> t.getToWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSent = transactions.stream()
            .filter(t -> t.getFromWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("\n--- TRANSACTION SUMMARY ---");
        logger.info("Total Transactions: {}", totalTransactions);
        logger.info("Successful: {} | Failed: {}", successfulTransactions, totalTransactions - successfulTransactions);
        logger.info("Total Received: ₹{}", totalReceived);
        logger.info("Total Sent: ₹{}", totalSent);
        logger.info("Net Revenue: ₹{}", totalReceived.subtract(totalSent));
    }

    private void displayRevenueReport(int walletId) {
        List<Transaction> transactions = transactionDAO.getTransactionsByWalletId(walletId);
        
        // Current month revenue
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        BigDecimal currentMonthRevenue = transactions.stream()
            .filter(t -> t.getToWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .filter(t -> t.getCreatedAt().isAfter(monthStart))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Today's revenue
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        BigDecimal todayRevenue = transactions.stream()
            .filter(t -> t.getToWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .filter(t -> t.getCreatedAt().isAfter(todayStart))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("\n--- REVENUE REPORT ---");
        logger.info("Today's Revenue: ₹{}", todayRevenue);
        logger.info("Current Month Revenue: ₹{}", currentMonthRevenue);
        logger.info("Period: {} to {}", monthStart.toLocalDate(), now.toLocalDate());
    }

    private void displayInvoiceSummary(int userId) {
        List<Invoice> invoices = invoiceDAO.getInvoicesByBusinessUserId(userId);
        
        long totalInvoices = invoices.size();
        long paidInvoices = invoices.stream().filter(i -> "PAID".equals(i.getStatus())).count();
        long unpaidInvoices = invoices.stream().filter(i -> "UNPAID".equals(i.getStatus())).count();
        
        BigDecimal totalInvoiceAmount = invoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal outstandingAmount = invoices.stream()
            .filter(i -> "UNPAID".equals(i.getStatus()))
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("\n--- INVOICE SUMMARY ---");
        logger.info("Total Invoices: {}", totalInvoices);
        logger.info("Paid: {} | Unpaid: {}", paidInvoices, unpaidInvoices);
        logger.info("Total Invoice Value: ₹{}", totalInvoiceAmount);
        logger.info("Outstanding Amount: ₹{}", outstandingAmount);
    }

    private void displayPaymentTrends(int walletId) {
        List<Transaction> transactions = transactionDAO.getTransactionsByWalletId(walletId);
        
        // Last 7 days transactions
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long last7DaysCount = transactions.stream()
            .filter(t -> t.getToWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .filter(t -> t.getCreatedAt().isAfter(weekAgo))
            .count();
        
        BigDecimal last7DaysRevenue = transactions.stream()
            .filter(t -> t.getToWalletId() == walletId && "SUCCESS".equals(t.getStatus()))
            .filter(t -> t.getCreatedAt().isAfter(weekAgo))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgTransactionValue = last7DaysCount > 0 
            ? last7DaysRevenue.divide(BigDecimal.valueOf(last7DaysCount), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        logger.info("\n--- PAYMENT TRENDS (Last 7 Days) ---");
        logger.info("Transactions: {}", last7DaysCount);
        logger.info("Revenue: ₹{}", last7DaysRevenue);
        logger.info("Average Transaction Value: ₹{}", avgTransactionValue);
    }

    private void displayTopCustomers(int userId) {
        List<Invoice> invoices = invoiceDAO.getInvoicesByBusinessUserId(userId);
        
        // Group by customer and sum amounts
        Map<String, BigDecimal> customerRevenue = invoices.stream()
            .filter(i -> "PAID".equals(i.getStatus()))
            .collect(Collectors.groupingBy(
                Invoice::getCustomerIdentifier,
                Collectors.mapping(Invoice::getTotalAmount, 
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
        
        // Sort and get top 5
        List<Map.Entry<String, BigDecimal>> topCustomers = customerRevenue.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(5)
            .toList();

        logger.info("\n--- TOP CUSTOMERS ---");
        if (topCustomers.isEmpty()) {
            logger.info("No customer data available yet");
        } else {
            int rank = 1;
            for (Map.Entry<String, BigDecimal> entry : topCustomers) {
                logger.info("{}. {} - ₹{}", rank++, entry.getKey(), entry.getValue());
            }
        }
    }
}
