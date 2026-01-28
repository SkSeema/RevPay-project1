package org.example.service;

import org.example.dao.LoanDAO;
import org.example.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanDAO loanDAO;

    private LoanService loanService;

    private Loan testLoan;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanDAO);
        testLoan = new Loan();
        testLoan.setLoanId(1);
        testLoan.setBusinessUserId(1);
        testLoan.setLoanAmount(new BigDecimal("10000.00"));
        testLoan.setRepaymentAmount(new BigDecimal("11000.00"));
        testLoan.setPurpose("Business Expansion");
        testLoan.setStatus("APPLIED");
    }

    @Test
    void testApplyForLoan_Success() {
        // Arrange
        when(loanDAO.applyForLoan(any(Loan.class))).thenReturn(1);

        // Act
        int loanId = loanService.applyForLoan(1, new BigDecimal("10000.00"), "Business Expansion");

        // Assert
        assertEquals(1, loanId);
        verify(loanDAO, times(1)).applyForLoan(any(Loan.class));
    }

    @Test
    void testApplyForLoan_CalculatesInterest() {
        // Arrange
        when(loanDAO.applyForLoan(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            // Verify 10% interest calculation
            BigDecimal expectedRepayment = loan.getLoanAmount().multiply(new BigDecimal("1.10"));
            assertEquals(0, expectedRepayment.compareTo(loan.getRepaymentAmount()));
            return 1;
        });

        // Act
        loanService.applyForLoan(1, new BigDecimal("10000.00"), "Equipment Purchase");

        // Assert
        verify(loanDAO, times(1)).applyForLoan(any(Loan.class));
    }

    @Test
    void testGetBusinessLoans() {
        // Arrange
        List<Loan> loans = new ArrayList<>();
        loans.add(testLoan);
        when(loanDAO.getLoansByBusinessUserId(1)).thenReturn(loans);

        // Act
        List<Loan> result = loanService.getBusinessLoans(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loanDAO, times(1)).getLoansByBusinessUserId(1);
    }

    @Test
    void testGetLoanById() {
        // Arrange
        when(loanDAO.getLoanById(1)).thenReturn(testLoan);

        // Act
        Loan result = loanService.getLoanById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLoanId());
        verify(loanDAO, times(1)).getLoanById(1);
    }

    @Test
    void testUpdateLoanStatus() {
        // Arrange
        when(loanDAO.updateLoanStatus(1, "APPROVED")).thenReturn(true);

        // Act
        boolean result = loanService.updateLoanStatus(1, "APPROVED");

        // Assert
        assertTrue(result);
        verify(loanDAO, times(1)).updateLoanStatus(1, "APPROVED");
    }

    @Test
    void testGetPendingLoans() {
        // Arrange
        List<Loan> allLoans = new ArrayList<>();
        testLoan.setStatus("APPLIED");
        allLoans.add(testLoan);
        
        Loan approvedLoan = new Loan();
        approvedLoan.setStatus("APPROVED");
        allLoans.add(approvedLoan);
        
        when(loanDAO.getLoansByBusinessUserId(1)).thenReturn(allLoans);

        // Act
        List<Loan> result = loanService.getPendingLoans(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("APPLIED", result.get(0).getStatus());
    }

    @Test
    void testGetApprovedLoans() {
        // Arrange
        List<Loan> allLoans = new ArrayList<>();
        
        Loan approvedLoan = new Loan();
        approvedLoan.setLoanId(2);
        approvedLoan.setBusinessUserId(1);
        approvedLoan.setStatus("APPROVED");
        allLoans.add(approvedLoan);
        
        testLoan.setStatus("REJECTED");
        allLoans.add(testLoan);
        
        when(loanDAO.getLoansByBusinessUserId(1)).thenReturn(allLoans);

        // Act
        List<Loan> result = loanService.getApprovedLoans(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("APPROVED", result.get(0).getStatus());
    }
}
