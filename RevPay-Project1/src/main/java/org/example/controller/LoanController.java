package org.example.controller;

import org.example.model.Loan;
import org.example.service.LoanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class LoanController {

    private static final Logger logger = LogManager.getLogger(LoanController.class);
    private final LoanService loanService = new LoanService();
    private final Scanner scanner = new Scanner(System.in);


    public void manageLoans(int businessUserId) {
        while (true) {
            logger.info("\n======= BUSINESS LOAN MANAGEMENT =======");
            logger.info("1. Apply for New Loan");
            logger.info("2. View All Loan Applications");
            logger.info("3. View Pending Loans");
            logger.info("4. View Approved Loans");
            logger.info("5. View Loan Details");
            logger.info("6. Back to Dashboard");
            logger.info("========================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> applyForLoan(businessUserId);
                case "2" -> viewAllLoans(businessUserId);
                case "3" -> viewPendingLoans(businessUserId);
                case "4" -> viewApprovedLoans(businessUserId);
                case "5" -> viewLoanDetails();
                case "6" -> {
                    logger.info("Returning to dashboard...");
                    return;
                }
                default -> logger.warn("Invalid option. Please try again.");
            }
        }
    }

    private void applyForLoan(int businessUserId) {
        logger.info("\n--- APPLY FOR BUSINESS LOAN ---");

        System.out.print("Enter loan amount: ₹");
        BigDecimal loanAmount;
        try {
            loanAmount = new BigDecimal(scanner.nextLine());
            if (loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.warn("Loan amount must be positive");
                return;
            }
            if (loanAmount.compareTo(new BigDecimal("10000000")) > 0) {
                logger.warn("Loan amount exceeds maximum limit of ₹1,00,00,000");
                return;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid amount");
            return;
        }

        System.out.print("Enter loan purpose (e.g., Business Expansion, Equipment Purchase): ");
        String purpose = scanner.nextLine();

        if (purpose.trim().isEmpty()) {
            logger.warn("Loan purpose cannot be empty");
            return;
        }


        BigDecimal interest = loanAmount.multiply(BigDecimal.valueOf(0.10));
        BigDecimal repayment = loanAmount.add(interest);

        logger.info("\n--- LOAN SUMMARY ---");
        logger.info("Loan Amount: ₹{}", loanAmount);
        logger.info("Interest Rate: 10%");
        logger.info("Interest Amount: ₹{}", interest);
        logger.info("Total Repayment: ₹{}", repayment);
        logger.info("Purpose: {}", purpose);

        System.out.print("\nConfirm loan application? (y/n): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("y")) {
            int loanId = loanService.applyForLoan(businessUserId, loanAmount, purpose);
            if (loanId > 0) {
                logger.info("✓ Loan application submitted successfully! Application ID: {}", loanId);
                logger.info("Your application will be reviewed within 3-5 business days.");
            } else {
                logger.error("✗ Failed to submit loan application");
            }
        } else {
            logger.info("Loan application cancelled");
        }
    }

    private void viewAllLoans(int businessUserId) {
        List<Loan> loans = loanService.getBusinessLoans(businessUserId);
        displayLoanList(loans, "ALL LOAN APPLICATIONS");
    }

    private void viewPendingLoans(int businessUserId) {
        List<Loan> loans = loanService.getPendingLoans(businessUserId);
        displayLoanList(loans, "PENDING LOAN APPLICATIONS");
    }

    private void viewApprovedLoans(int businessUserId) {
        List<Loan> loans = loanService.getApprovedLoans(businessUserId);
        displayLoanList(loans, "APPROVED LOANS");
    }

    private void displayLoanList(List<Loan> loans, String title) {
        logger.info("\n--- {} ---", title);
        if (loans.isEmpty()) {
            logger.info("No loan applications found");
            return;
        }

        for (Loan loan : loans) {
            logger.info("Loan ID: {} | Amount: ₹{} | Repayment: ₹{} | Status: {} | Purpose: {}", 
                loan.getLoanId(), 
                loan.getLoanAmount(),
                loan.getRepaymentAmount(),
                loan.getStatus(), 
                loan.getPurpose());
        }
        logger.info("Total: {} loan(s)", loans.size());
    }

    private void viewLoanDetails() {
        System.out.print("Enter loan ID: ");
        int loanId;
        try {
            loanId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            logger.warn("Invalid loan ID");
            return;
        }

        Loan loan = loanService.getLoanById(loanId);
        if (loan == null) {
            logger.warn("Loan not found");
            return;
        }

        logger.info("\n========= LOAN DETAILS =========");
        logger.info("Loan ID: {}", loan.getLoanId());
        logger.info("Loan Amount: ₹{}", loan.getLoanAmount());
        logger.info("Repayment Amount: ₹{}", loan.getRepaymentAmount());
        logger.info("Interest: ₹{}", loan.getRepaymentAmount().subtract(loan.getLoanAmount()));
        logger.info("Purpose: {}", loan.getPurpose());
        logger.info("Status: {}", loan.getStatus());
        logger.info("Applied On: {}", loan.getCreatedAt());
        logger.info("================================");
    }
}
