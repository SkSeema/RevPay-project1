package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

public class PaymentMethod {

    private static final Logger logger = LogManager.getLogger(PaymentMethod.class);

    private int paymentId;
    private int userId;
    private String methodType; // CARD / BANK
    private String encryptedDetails;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public PaymentMethod() {
        logger.debug("PaymentMethod object created");
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getEncryptedDetails() {
        return encryptedDetails;
    }

    public void setEncryptedDetails(String encryptedDetails) {
        this.encryptedDetails = encryptedDetails;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
