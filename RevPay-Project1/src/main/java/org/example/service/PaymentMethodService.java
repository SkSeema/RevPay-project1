package org.example.service;

import org.example.dao.PaymentMethodDAO;
import org.example.model.PaymentMethod;
import org.example.security.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class PaymentMethodService {

    private static final Logger logger = LogManager.getLogger(PaymentMethodService.class);
    private final PaymentMethodDAO paymentMethodDAO;

    // Default constructor for production use
    public PaymentMethodService() {
        this.paymentMethodDAO = new PaymentMethodDAO();
    }

    // Constructor for testing with dependency injection
    public PaymentMethodService(PaymentMethodDAO paymentMethodDAO) {
        this.paymentMethodDAO = paymentMethodDAO;
    }

    // Add card details
    public boolean addCard(int userId, String cardType, String cardNumber, String cardHolderName, String expiryDate, String cvv, boolean isDefault) {
        try {
            // Mask card number for display (show only last 4 digits)
            String maskedCardNumber = maskCardNumber(cardNumber);
            
            // Encrypt card details
            String cardDetails = String.format("%s|%s|%s|%s|%s", cardType, cardNumber, cardHolderName, expiryDate, cvv);
            String encryptedDetails = EncryptionUtil.encrypt(cardDetails);

            PaymentMethod pm = new PaymentMethod();
            pm.setUserId(userId);
            pm.setMethodType("CARD");
            pm.setEncryptedDetails(encryptedDetails);
            pm.setDefault(isDefault);

            boolean added = paymentMethodDAO.addPaymentMethod(pm);
            
            if (added && isDefault) {
                // If this is set as default, get the payment ID and update
                List<PaymentMethod> methods = paymentMethodDAO.getPaymentMethodsByUserId(userId);
                if (!methods.isEmpty()) {
                    PaymentMethod latest = methods.get(0);
                    paymentMethodDAO.setDefaultPaymentMethod(userId, latest.getPaymentId());
                }
            }
            
            return added;
        } catch (Exception e) {
            logger.error("Error adding card for userId={}", userId, e);
            return false;
        }
    }

    // Get all payment methods for user
    public List<PaymentMethod> getUserPaymentMethods(int userId) {
        return paymentMethodDAO.getPaymentMethodsByUserId(userId);
    }

    // Get decrypted card details
    public String getDecryptedCardDetails(int paymentId) {
        PaymentMethod pm = paymentMethodDAO.getPaymentMethodById(paymentId);
        if (pm != null) {
            try {
                return EncryptionUtil.decrypt(pm.getEncryptedDetails());
            } catch (Exception e) {
                logger.error("Error decrypting card details for paymentId={}", paymentId, e);
            }
        }
        return null;
    }

    // Get masked card info for display
    public String getMaskedCardInfo(PaymentMethod pm) {
        try {
            String decrypted = EncryptionUtil.decrypt(pm.getEncryptedDetails());
            String[] parts = decrypted.split("\\|");
            if (parts.length >= 3) {
                String cardType = parts[0];
                String cardNumber = parts[1];
                String maskedNumber = maskCardNumber(cardNumber);
                return cardType + " " + maskedNumber;
            }
        } catch (Exception e) {
            logger.error("Error getting masked card info", e);
        }
        return "Card ****";
    }

    // Set default payment method
    public boolean setDefaultCard(int userId, int paymentId) {
        return paymentMethodDAO.setDefaultPaymentMethod(userId, paymentId);
    }

    // Delete payment method
    public boolean deleteCard(int userId, int paymentId) {
        return paymentMethodDAO.deletePaymentMethod(paymentId, userId);
    }

    // Get default payment method
    public PaymentMethod getDefaultPaymentMethod(int userId) {
        return paymentMethodDAO.getDefaultPaymentMethod(userId);
    }

    // Mask card number (show only last 4 digits)
    private String maskCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }

    // Validate card number (simple length check)
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        
        // Remove spaces and dashes
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // Check if all digits
        if (!cardNumber.matches("\\d+")) {
            return false;
        }
        
        // Check length between 13-19 digits
        return cardNumber.length() >= 13 && cardNumber.length() <= 19;
    }
}
