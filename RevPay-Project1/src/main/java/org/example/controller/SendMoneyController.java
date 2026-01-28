package org.example.controller;

import org.example.service.SendMoneyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Scanner;

public class SendMoneyController {

    private static final Logger logger = LogManager.getLogger(SendMoneyController.class);
    private final SendMoneyService sendMoneyService = new SendMoneyService();
    private final Scanner sc = new Scanner(System.in);

    public void sendMoneyMenu(int senderUserId) {
        boolean running = true;
        while (running) {
            logger.info("===== SEND MONEY MENU =====");
            logger.info("1 → Send Money");
            logger.info("2 → Back to Wallet Menu");
            logger.info("Enter your choice: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    sendMoney(senderUserId);
                    break;
                case "2":
                    running = false;
                    break;
                default:
                    logger.warn("Invalid choice: {}", choice);
            }
        }
    }

    private void sendMoney(int senderUserId) {
        logger.info("Enter recipient username/email/phone:");
        String recipient = sc.nextLine();

        logger.info("Enter amount to send:");
        BigDecimal amount = new BigDecimal(sc.nextLine());

        logger.info("Optional note (press Enter to skip):");
        String note = sc.nextLine();

        boolean success = sendMoneyService.sendMoney(senderUserId, recipient, amount, note);

        if (success) {
            logger.info("Money sent successfully to {}. Amount: {}", recipient, amount);
        } else {
            logger.warn("Failed to send money. Check balance or recipient info.");
        }
    }
}
