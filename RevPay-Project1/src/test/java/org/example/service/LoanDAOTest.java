package org.example.service;

import org.example.dao.LoanDAO;
import org.example.config.DBConnection;
import org.example.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanDAOTest {

    private LoanDAO dao;

    @BeforeEach
    void setUp() {
        dao = new LoanDAO();
    }

    @Test
    void testApplyForLoan_success() throws SQLException {
        Loan loan = new Loan();
        loan.setBusinessUserId(1);
        loan.setLoanAmount(new BigDecimal("10000.00"));
        loan.setPurpose("Business Expansion");
        loan.setRepaymentAmount(new BigDecimal("11000.00"));

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);
        when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(101);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            int loanId = dao.applyForLoan(loan);

            assertEquals(101, loanId);
            verify(mockPs).setInt(1, loan.getBusinessUserId());
            verify(mockPs).setBigDecimal(2, loan.getLoanAmount());
            verify(mockPs).setString(3, loan.getPurpose());
            verify(mockPs).setString(4, "APPLIED"); // default status
            verify(mockPs).setBigDecimal(5, loan.getRepaymentAmount());
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetLoansByBusinessUserId_returnsLoans() throws SQLException {
        int userId = 1;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        // Mock two loans
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("loan_id")).thenReturn(101, 102);
        when(mockRs.getInt("business_user_id")).thenReturn(userId, userId);
        when(mockRs.getBigDecimal("loan_amount")).thenReturn(new BigDecimal("10000.00"), new BigDecimal("20000.00"));
        when(mockRs.getString("purpose")).thenReturn("Expansion", "Equipment");
        when(mockRs.getString("status")).thenReturn("APPLIED", "APPROVED");
        when(mockRs.getBigDecimal("repayment_amount")).thenReturn(new BigDecimal("11000.00"), new BigDecimal("22000.00"));
        when(mockRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2026-01-27 10:00:00"), Timestamp.valueOf("2026-01-27 11:00:00"));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<Loan> loans = dao.getLoansByBusinessUserId(userId);

            assertEquals(2, loans.size());
            assertEquals(101, loans.get(0).getLoanId());
            assertEquals("Expansion", loans.get(0).getPurpose());
            assertEquals("APPROVED", loans.get(1).getStatus());
        }
    }

    @Test
    void testGetLoanById_found() throws SQLException {
        int loanId = 101;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("loan_id")).thenReturn(loanId);
        when(mockRs.getInt("business_user_id")).thenReturn(1);
        when(mockRs.getBigDecimal("loan_amount")).thenReturn(new BigDecimal("10000.00"));
        when(mockRs.getString("purpose")).thenReturn("Expansion");
        when(mockRs.getString("status")).thenReturn("APPLIED");
        when(mockRs.getBigDecimal("repayment_amount")).thenReturn(new BigDecimal("11000.00"));
        when(mockRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2026-01-27 10:00:00"));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            Loan loan = dao.getLoanById(loanId);

            assertNotNull(loan);
            assertEquals(loanId, loan.getLoanId());
            assertEquals("Expansion", loan.getPurpose());
        }
    }

    @Test
    void testUpdateLoanStatus_success() throws SQLException {
        int loanId = 101;
        String status = "APPROVED";

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.updateLoanStatus(loanId, status);

            assertTrue(result);
            verify(mockPs).setString(1, status);
            verify(mockPs).setInt(2, loanId);
            verify(mockPs).executeUpdate();
        }
    }
}
