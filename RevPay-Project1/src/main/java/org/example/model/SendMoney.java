package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SendMoney {

    private int transactionId;       // Auto-increment transaction ID
    private int senderUserId;        // Who is sending
    private int receiverUserId;      // Who is receiving
    private BigDecimal amount;       // Amount to send
    private LocalDateTime timestamp; // Transaction timestamp
    private String status;           // SUCCESS / FAILED

    // ---------- Getters & Setters ----------

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }

    public int getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(int receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
