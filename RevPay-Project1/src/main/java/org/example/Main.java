package org.example;

import org.example.controller.*;
import org.example.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Scanner sc = new Scanner(System.in);
    private static final UserController userController = new UserController();
    private static final WalletController walletController = new WalletController();
    private static final SendMoneyController sendMoneyController = new SendMoneyController();
    private static final MoneyRequestController moneyRequestController = new MoneyRequestController();
    private static final PaymentMethodController paymentMethodController = new PaymentMethodController();
    private static final InvoiceController invoiceController = new InvoiceController();
    private static final LoanController loanController = new LoanController();
    private static final BusinessAnalyticsController analyticsController = new BusinessAnalyticsController();
    private static final NotificationController notificationController = new NotificationController();

    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            logger.info("========Welcome to REV-Pay Application=======");
            logger.info("===== REV-PAY MENU =====");
            logger.info("1 → Register");
            logger.info("2 → Login");
            logger.info("3 → Forgot Password");
            logger.info("4 → Exit");
            logger.info("Enter your choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> userController.registerUser();
                case "2" -> loginFlow();
                case "3" -> userController.forgotPassword();
                case "4" -> {
                    running = false;
                    logger.info("Exiting Rev-Pay. Goodbye!");
                }
                default -> logger.warn("Invalid choice. Try again.");
            }
        }
    }

    private static void loginFlow() {
        User loggedInUser = userController.loginUser();
        if (loggedInUser != null) {
            logger.info("LOGIN SUCCESSFUL. Welcome {}", loggedInUser.getFullName());
            postLoginMenu(loggedInUser);
        }
    }

    public static void postLoginMenu(User user) {
        boolean dashboardRunning = true;
        boolean isBusinessAccount = "BUSINESS".equalsIgnoreCase(user.getAccountType());

        while (dashboardRunning) {
            // Get unread notification count
            int unreadCount = notificationController.getUnreadCount(user.getUserId());
            String notificationBadge = unreadCount > 0 ? "  (" + unreadCount + ")" : "";
            
            if (isBusinessAccount) {
                logger.info("===== BUSINESS DASHBOARD =====");
            } else {
                logger.info("===== USER DASHBOARD =====");
            }
            
            logger.info("1 → Show Wallet Balance");
            logger.info("2 → Deposit");
            logger.info("3 → Withdraw");
            logger.info("4 → Send Money");
            logger.info("5 → Request Money");
            logger.info("6 → Transaction History");
            logger.info("7 → Transaction History (With Filters)");
            logger.info("8 → Payment Methods (Cards)");
            logger.info("0 → Notifications{}", notificationBadge);
            
            // Business-specific options
            if (isBusinessAccount) {
                logger.info("10 → Invoice Management");
                logger.info("11 → Business Loans");
                logger.info("12 → Business Analytics");
            }
            
            logger.info("9 → Logout");
            logger.info("Enter your choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "0" -> notificationController.viewNotifications(user.getUserId());
                case "1" -> walletController.showWallet(user.getUserId());
                case "2" -> walletController.deposit(user.getUserId());
                case "3" -> walletController.withdraw(user.getUserId());
                case "4" -> sendMoneyController.sendMoneyMenu(user.getUserId());
                case "5" -> moneyRequestController.moneyRequestMenu(user.getUserId());
                case "6" -> walletController.viewTransactionHistory(user.getUserId());
                case "7" -> walletController.viewTransactionHistoryWithFilters(user.getUserId());
                case "8" -> paymentMethodController.paymentMethodMenu(user.getUserId());
                case "10" -> {
                    if (isBusinessAccount) {
                        invoiceController.manageInvoices(user.getUserId());
                    } else {
                        logger.warn("This feature is only available for business accounts");
                    }
                }
                case "11" -> {
                    if (isBusinessAccount) {
                        loanController.manageLoans(user.getUserId());
                    } else {
                        logger.warn("This feature is only available for business accounts");
                    }
                }
                case "12" -> {
                    if (isBusinessAccount) {
                        analyticsController.viewAnalytics(user.getUserId());
                    } else {
                        logger.warn("This feature is only available for business accounts");
                    }
                }
                case "9" -> {
                    dashboardRunning = false;
                    logger.info("Logging out...");
                }
                default -> logger.warn("Invalid choice. Try again.");
            }
        }
    }
}
