package org.example.controller;

import org.example.model.MoneyRequest;
import org.example.service.MoneyRequestService;
import org.example.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class MoneyRequestController {

    private static final Logger logger = LogManager.getLogger(MoneyRequestController.class);
    private final Scanner sc = new Scanner(System.in);
    private final MoneyRequestService requestService = new MoneyRequestService();
    private final UserService userService = new UserService();

    public void moneyRequestMenu(int loggedInUserId) {
        boolean running = true;
        while (running) {
            logger.info("===== MONEY REQUEST MENU =====");
            logger.info("1 → Request Money");
            logger.info("2 → View Received Requests");
            logger.info("3 → Back");
            logger.info("Enter your choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> requestMoney(loggedInUserId);
                case "2" -> viewRequests(loggedInUserId);
                case "3" -> running = false;
                default -> logger.warn("Invalid choice!");
            }
        }
    }

    private void requestMoney(int loggedInUserId) {
        logger.info("Enter username/email/phone: ");
        String input = sc.nextLine();
        Integer receiverId = userService.getUserIdByIdentifier(input);
        if (receiverId == null) {
            logger.warn("User not found!");
            return;
        }
        logger.info("Enter amount: ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        logger.info("Optional note (press Enter to skip): ");
        String note = sc.nextLine();

        MoneyRequest r = new MoneyRequest();
        r.setFromUserId(loggedInUserId);
        r.setToUserId(receiverId);
        r.setAmount(amount);
        r.setNote(note.isEmpty() ? null : note);

        if (requestService.sendRequest(r)) {
            logger.info("Money request sent successfully");
        } else {
            logger.warn("Failed to send request");
        }
    }

    private void viewRequests(int loggedInUserId) {
        List<MoneyRequest> list = requestService.getRequestsForUser(loggedInUserId);
        if (list.isEmpty()) {
            logger.info("No requests found");
            return;
        }

        for (MoneyRequest r : list) {
            String fromName = userService.getUserNameById(r.getFromUserId());
            logger.info("ID:{} From:{} Amount:{} Status:{} Note:{}", 
                    r.getRequestId(), fromName, r.getAmount(), r.getStatus(),
                    r.getNote() == null ? "" : r.getNote());
        }

        logger.info("Enter Request ID to ACCEPT / DECLINE / CANCEL or 0 to go back: ");
        String input = sc.nextLine();
        if (input.equals("0")) return;

        try {
            int reqId = Integer.parseInt(input);
            logger.info("Type ACCEPT / DECLINE / CANCEL: ");
            String action = sc.nextLine().toUpperCase();

            if (!action.equals("ACCEPT") && !action.equals("DECLINE") && !action.equals("CANCEL")) {
                logger.warn("Invalid action!");
                return;
            }


            String dbStatus = switch (action) {
                case "ACCEPT" -> "ACCEPTED";
                case "DECLINE" -> "DECLINED";
                case "CANCEL" -> "CANCELED";
                default -> action;
            };

            boolean updated = requestService.updateRequestStatus(reqId, dbStatus);
            logger.info("Request status updated, rows={}", (updated ? 1 : 0));

        } catch (NumberFormatException e) {
            logger.warn("Invalid Request ID input");
        }
    }
}
