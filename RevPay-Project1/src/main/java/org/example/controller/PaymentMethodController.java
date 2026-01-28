package org.example.controller;

import org.example.model.PaymentMethod;
import org.example.service.NotificationService;
import org.example.service.PaymentMethodService;
import org.example.service.WalletService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class PaymentMethodController {

    private static final Logger logger = LogManager.getLogger(PaymentMethodController.class);
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private final WalletService walletService = new WalletService();
    private final NotificationService notificationService = new NotificationService();
    private final Scanner sc = new Scanner(System.in);

    public void paymentMethodMenu(int userId) {
        boolean running = true;
        while (running) {
            logger.info("\n===== PAYMENT METHODS MENU =====");
            logger.info("1 → Add Card");
            logger.info("2 → View My Cards");
            logger.info("3 → Set Default Card");
            logger.info("4 → Delete Card");
            logger.info("5 → Deposit using Card");
            logger.info("6 → Back");
            logger.info("Enter choice:");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> addCard(userId);
                case "2" -> viewCards(userId);
                case "3" -> setDefaultCard(userId);
                case "4" -> deleteCard(userId);
                case "5" -> depositUsingCard(userId);
                case "6" -> running = false;
                default -> logger.warn("Invalid choice. Try again.");
            }
        }
    }

    private void addCard(int userId) {
        logger.info("\n===== ADD CARD =====");
        logger.info("Enter card type (CREDIT/DEBIT):");
        String cardType = sc.nextLine().toUpperCase();
        
        if (!cardType.equals("CREDIT") && !cardType.equals("DEBIT")) {
            logger.warn("Invalid card type. Please enter CREDIT or DEBIT.");
            return;
        }

        logger.info("Enter card number (13-19 digits):");
        String cardNumber = sc.nextLine().replaceAll("[\\s-]", "");
        
        if (!paymentMethodService.validateCardNumber(cardNumber)) {
            logger.warn("Invalid card number. Please check and try again.");
            return;
        }

        logger.info("Enter card holder name:");
        String cardHolderName = sc.nextLine();

        logger.info("Enter expiry date (MM/YY):");
        String expiryDate = sc.nextLine();

        logger.info("Enter CVV:");
        String cvv = sc.nextLine();

        logger.info("Set as default card? (yes/no):");
        String defaultChoice = sc.nextLine().toLowerCase();
        boolean isDefault = defaultChoice.equals("yes") || defaultChoice.equals("y");

        boolean success = paymentMethodService.addCard(userId, cardType, cardNumber, cardHolderName, expiryDate, cvv, isDefault);
        
        if (success) {
            logger.info("Card added successfully!");
            if (isDefault) {
                logger.info("Card set as default payment method.");
            }

            String last4 = cardNumber.substring(cardNumber.length() - 4);
            notificationService.notifyCardChange(userId, "Added", last4);
        } else {
            logger.warn("Failed to add card. Please try again.");
        }
    }

    private void viewCards(int userId) {
        logger.info("\n===== MY CARDS =====");
        List<PaymentMethod> methods = paymentMethodService.getUserPaymentMethods(userId);
        
        if (methods.isEmpty()) {
            logger.info("No payment methods found. Add a card to get started.");
            return;
        }

        for (PaymentMethod pm : methods) {
            String maskedInfo = paymentMethodService.getMaskedCardInfo(pm);
            String defaultTag = pm.isDefault() ? " [DEFAULT]" : "";
            logger.info("ID: {} | {} | Added: {}{}", pm.getPaymentId(), maskedInfo, pm.getCreatedAt().toLocalDate(), defaultTag);
        }
    }

    private void setDefaultCard(int userId) {
        viewCards(userId);
        List<PaymentMethod> methods = paymentMethodService.getUserPaymentMethods(userId);
        
        if (methods.isEmpty()) {
            return;
        }

        logger.info("\nEnter card ID to set as default:");
        try {
            int paymentId = Integer.parseInt(sc.nextLine());
            
            // Get card info before setting default
            PaymentMethod selectedCard = methods.stream()
                .filter(pm -> pm.getPaymentId() == paymentId)
                .findFirst()
                .orElse(null);
            
            boolean success = paymentMethodService.setDefaultCard(userId, paymentId);
            
            if (success) {
                logger.info("Default card updated successfully!");

                if (selectedCard != null) {
                    String maskedInfo = paymentMethodService.getMaskedCardInfo(selectedCard);

                    String last4 = maskedInfo.substring(maskedInfo.length() - 4);
                    notificationService.notifyCardChange(userId, "Set as Default", last4);
                }
            } else {
                logger.warn("Failed to set default card. Please check the card ID.");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid card ID.");
        }
    }

    private void deleteCard(int userId) {
        viewCards(userId);
        List<PaymentMethod> methods = paymentMethodService.getUserPaymentMethods(userId);
        
        if (methods.isEmpty()) {
            return;
        }

        logger.info("\nEnter card ID to delete:");
        try {
            int paymentId = Integer.parseInt(sc.nextLine());
            

            PaymentMethod cardToDelete = methods.stream()
                .filter(pm -> pm.getPaymentId() == paymentId)
                .findFirst()
                .orElse(null);
            
            logger.info("Are you sure you want to delete this card? (yes/no):");
            String confirm = sc.nextLine().toLowerCase();
            
            if (confirm.equals("yes") || confirm.equals("y")) {
                boolean success = paymentMethodService.deleteCard(userId, paymentId);
                if (success) {
                    logger.info("Card deleted successfully!");

                    if (cardToDelete != null) {
                        String maskedInfo = paymentMethodService.getMaskedCardInfo(cardToDelete);

                        // Extract last 4 digits from masked info (format: "CARD_TYPE ****1234")
                        String last4 = maskedInfo.substring(maskedInfo.length() - 4);
                        notificationService.notifyCardChange(userId, "Deleted", last4);
                    }
                } else {
                    logger.warn("Failed to delete card.");
                }
            } else {
                logger.info("Deletion cancelled.");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid card ID.");
        }
    }

    private void depositUsingCard(int userId) {
        logger.info("\n===== DEPOSIT USING CARD =====");
        List<PaymentMethod> methods = paymentMethodService.getUserPaymentMethods(userId);
        
        if (methods.isEmpty()) {
            logger.info("No cards found. Please add a card first.");
            return;
        }


        logger.info("Available cards:");
        for (PaymentMethod pm : methods) {
            String maskedInfo = paymentMethodService.getMaskedCardInfo(pm);
            String defaultTag = pm.isDefault() ? " [DEFAULT]" : "";
            logger.info("ID: {} | {}{}", pm.getPaymentId(), maskedInfo, defaultTag);
        }

        logger.info("\nEnter card ID to use (or press Enter for default card):");
        String input = sc.nextLine();
        
        PaymentMethod selectedCard;
        if (input.isEmpty()) {
            selectedCard = paymentMethodService.getDefaultPaymentMethod(userId);
            if (selectedCard == null) {
                logger.warn("No default card set. Please select a card.");
                return;
            }
        } else {
            try {
                int paymentId = Integer.parseInt(input);
                selectedCard = methods.stream()
                    .filter(pm -> pm.getPaymentId() == paymentId)
                    .findFirst()
                    .orElse(null);
                
                if (selectedCard == null) {
                    logger.warn("Invalid card ID.");
                    return;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid input.");
                return;
            }
        }

        logger.info("Enter amount to deposit:");
        try {
            BigDecimal amount = new BigDecimal(sc.nextLine());
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Amount must be greater than zero.");
                return;
            }


            logger.info("Processing payment using {}...", paymentMethodService.getMaskedCardInfo(selectedCard));
            

            boolean success = walletService.deposit(userId, amount);
            
            if (success) {
                logger.info("Deposit successful! Amount ₹{} added to your wallet.", amount);
                logger.info("New balance: ₹{}", walletService.getWallet(userId).getBalance());
            } else {
                logger.warn("Deposit failed. Please try again.");
            }
            
        } catch (NumberFormatException e) {
            logger.warn("Invalid amount.");
        }
    }
}
