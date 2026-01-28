package org.example.controller;

import org.example.service.BusinessAnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class BusinessAnalyticsController {

    private static final Logger logger = LogManager.getLogger(BusinessAnalyticsController.class);
    private final BusinessAnalyticsService analyticsService = new BusinessAnalyticsService();
    private final Scanner scanner = new Scanner(System.in);


    public void viewAnalytics(int businessUserId) {
        while (true) {
            logger.info("\n======= BUSINESS ANALYTICS =======");
            logger.info("1. View Complete Analytics Dashboard");
            logger.info("2. Back to Dashboard");
            logger.info("===================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> analyticsService.displayBusinessAnalytics(businessUserId);
                case "2" -> {
                    logger.info("Returning to dashboard...");
                    return;
                }
                default -> logger.warn("Invalid option. Please try again.");
            }
        }
    }
}
