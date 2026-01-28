package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Loan {

    private static final Logger logger = LogManager.getLogger(Loan.class);

    private int loanId;
    private int businessUserId;
    private BigDecimal loanAmount;
    private String purpose;
    private String status; // APPLIED / APPROVED / REJECTED
    private BigDecimal repaymentAmount;
    private LocalDateTime createdAt;

    public Loan() {
        logger.debug("Loan object created");
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getBusinessUserId() {
        return businessUserId;
    }

    public void setBusinessUserId(int businessUserId) {
        this.businessUserId = businessUserId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
