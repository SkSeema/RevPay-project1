package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private static final Logger logger = LogManager.getLogger(Transaction.class);

    private int transactionId;
    private int fromWalletId;
    private int toWalletId;
    private BigDecimal amount;
    private String transactionType; // SEND, ADD, WITHDRAW
    private String status;          // SUCCESS, FAILED, PENDING
    private String note;
    private LocalDateTime createdAt;

    public Transaction() {
        logger.debug("Transaction object created");
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getFromWalletId() {
        return fromWalletId;
    }

    public void setFromWalletId(int fromWalletId) {
        this.fromWalletId = fromWalletId;
    }

    public int getToWalletId() {
        return toWalletId;
    }

    public void setToWalletId(int toWalletId) {
        this.toWalletId = toWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
