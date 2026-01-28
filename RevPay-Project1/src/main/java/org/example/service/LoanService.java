package org.example.service;

import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;

public class LoanService {

    private static final Logger logger = LogManager.getLogger(LoanService.class);
    private final LoanDAO loanDAO;

    // Default constructor for production use
    public LoanService() {
        this.loanDAO = new LoanDAO();
    }

    // Constructor for testing with dependency injection
    public LoanService(LoanDAO loanDAO) {
        this.loanDAO = loanDAO;
    }

    // Apply for business loan
    public int applyForLoan(int businessUserId, BigDecimal loanAmount, String purpose) {
        // Calculate repayment amount (simple interest calculation: 10% interest)
        BigDecimal interest = loanAmount.multiply(BigDecimal.valueOf(0.10));
        BigDecimal repaymentAmount = loanAmount.add(interest);

        Loan loan = new Loan();
        loan.setBusinessUserId(businessUserId);
        loan.setLoanAmount(loanAmount);
        loan.setPurpose(purpose);
        loan.setStatus("APPLIED");
        loan.setRepaymentAmount(repaymentAmount);

        int loanId = loanDAO.applyForLoan(loan);
        logger.info("Loan application submitted: loanId={}, amount={}, repayment={}", 
            loanId, loanAmount, repaymentAmount);
        return loanId;
    }

    // Get all loans for business
    public List<Loan> getBusinessLoans(int businessUserId) {
        return loanDAO.getLoansByBusinessUserId(businessUserId);
    }

    // Get loan by ID
    public Loan getLoanById(int loanId) {
        return loanDAO.getLoanById(loanId);
    }

    // Update loan status (admin function - simulated)
    public boolean updateLoanStatus(int loanId, String status) {
        return loanDAO.updateLoanStatus(loanId, status);
    }

    // Get pending loans
    public List<Loan> getPendingLoans(int businessUserId) {
        return getBusinessLoans(businessUserId).stream()
            .filter(loan -> "APPLIED".equals(loan.getStatus()))
            .toList();
    }

    // Get approved loans
    public List<Loan> getApprovedLoans(int businessUserId) {
        return getBusinessLoans(businessUserId).stream()
            .filter(loan -> "APPROVED".equals(loan.getStatus()))
            .toList();
    }
}
