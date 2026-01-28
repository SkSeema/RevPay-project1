package org.example.service;

import org.example.dao.MoneyRequestDAO;
import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.MoneyRequest;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.model.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MoneyRequestService {
    private final MoneyRequestDAO dao;
    private final WalletDAO walletDAO;
    private final TransactionDAO transactionDAO;
    private final NotificationService notificationService;
    private final UserDAO userDAO;
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("500");

    // Default constructor for production use
    public MoneyRequestService() {
        this.dao = new MoneyRequestDAO();
        this.walletDAO = new WalletDAO();
        this.transactionDAO = new TransactionDAO();
        this.notificationService = new NotificationService();
        this.userDAO = new UserDAO();
    }

    // Constructor for testing with dependency injection
    public MoneyRequestService(MoneyRequestDAO dao, WalletDAO walletDAO, TransactionDAO transactionDAO, NotificationService notificationService, UserDAO userDAO) {
        this.dao = dao;
        this.walletDAO = walletDAO;
        this.transactionDAO = transactionDAO;
        this.notificationService = notificationService;
        this.userDAO = userDAO;
    }

    public boolean sendRequest(MoneyRequest request) {
        boolean created = dao.createRequest(request);
        if (created) {
            // Notify the person being requested
            User requester = userDAO.getUserById(request.getFromUserId());
            if (requester != null) {
                notificationService.notifyMoneyRequest(request.getToUserId(), "Money Request", 
                    request.getAmount(), requester.getFullName());
            }
        }
        return created;
    }

    public List<MoneyRequest> getRequestsForUser(int userId) {
        return dao.getRequestsForUser(userId);
    }

    public boolean updateRequestStatus(int requestId, String status) {
        boolean updated = dao.updateRequestStatus(requestId, status);
        
        // If request is ACCEPTED, transfer money from requestee (to_user) to requester (from_user)
        if (updated && "ACCEPTED".equals(status)) {
            MoneyRequest request = dao.getRequestById(requestId);
            if (request != null) {
                // Transfer money: from_user requested money from to_user
                // So to_user sends money to from_user
                boolean transferSuccess = walletDAO.transfer(request.getToUserId(), request.getFromUserId(), request.getAmount());
                
                // Log transaction
                if (transferSuccess) {
                    Wallet fromWallet = walletDAO.getWalletByUserId(request.getToUserId());
                    Wallet toWallet = walletDAO.getWalletByUserId(request.getFromUserId());
                    
                    if (fromWallet != null && toWallet != null) {
                        Transaction transaction = new Transaction();
                        transaction.setFromWalletId(fromWallet.getWalletId());
                        transaction.setToWalletId(toWallet.getWalletId());
                        transaction.setAmount(request.getAmount());
                        transaction.setTransactionType("SEND");
                        transaction.setStatus("SUCCESS");
                        transaction.setNote("Money request accepted: " + (request.getNote() != null ? request.getNote() : ""));
                        transaction.setCreatedAt(LocalDateTime.now());
                        transactionDAO.logTransaction(transaction);
                        
                        // Get user names
                        User requester = userDAO.getUserById(request.getFromUserId());
                        User accepter = userDAO.getUserById(request.getToUserId());
                        
                        // Notify requester that request was accepted
                        if (requester != null && accepter != null) {
                            notificationService.notifyMoneyRequest(request.getFromUserId(), "Request Accepted", 
                                request.getAmount(), accepter.getFullName());
                            
                            // Notify accepter about transaction
                            notificationService.notifyTransaction(request.getToUserId(), "Money Sent", 
                                request.getAmount(), "Request accepted for " + requester.getFullName());
                        }
                        
                        // Check accepter's balance for low balance alert
                        Wallet updatedWallet = walletDAO.getWalletByUserId(request.getToUserId());
                        if (updatedWallet != null && updatedWallet.getBalance().compareTo(LOW_BALANCE_THRESHOLD) < 0) {
                            notificationService.notifyLowBalance(request.getToUserId(), updatedWallet.getBalance());
                        }
                    }
                }
            }
        } else if (updated && "REJECTED".equals(status)) {
            // Notify requester that request was rejected
            MoneyRequest request = dao.getRequestById(requestId);
            if (request != null) {
                User rejecter = userDAO.getUserById(request.getToUserId());
                if (rejecter != null) {
                    notificationService.notifyMoneyRequest(request.getFromUserId(), "Request Rejected", 
                        request.getAmount(), rejecter.getFullName());
                }
            }
        }
        
        return updated;
    }
}
