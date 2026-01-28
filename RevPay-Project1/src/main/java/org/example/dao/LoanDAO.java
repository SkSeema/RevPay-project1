package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.Loan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private static final Logger logger = LogManager.getLogger(LoanDAO.class);


    public int applyForLoan(Loan loan) {
        String sql = "INSERT INTO loans (business_user_id, loan_amount, purpose, status, repayment_amount) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBusinessUserId());
            ps.setBigDecimal(2, loan.getLoanAmount());
            ps.setString(3, loan.getPurpose());
            ps.setString(4, loan.getStatus() != null ? loan.getStatus() : "APPLIED");
            ps.setBigDecimal(5, loan.getRepaymentAmount());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int loanId = rs.getInt(1);
                    logger.info("Loan application created: loanId={}", loanId);
                    return loanId;
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error applying for loan", e);
            return 0;
        }
    }


    public List<Loan> getLoansByBusinessUserId(int businessUserId) {
        String sql = "SELECT * FROM loans WHERE business_user_id = ? ORDER BY created_at DESC";
        List<Loan> loans = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Loan loan = mapResultSetToLoan(rs);
                loans.add(loan);
            }
            logger.info("Retrieved {} loans for businessUserId={}", loans.size(), businessUserId);
        } catch (SQLException e) {
            logger.error("Error fetching loans", e);
        }
        return loans;
    }


    public Loan getLoanById(int loanId) {
        String sql = "SELECT * FROM loans WHERE loan_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToLoan(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching loan by ID={}", loanId, e);
        }
        return null;
    }


    public boolean updateLoanStatus(int loanId, String status) {
        String sql = "UPDATE loans SET status = ? WHERE loan_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, loanId);
            int rows = ps.executeUpdate();
            logger.info("Loan status updated: loanId={}, status={}, rows={}", loanId, status, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating loan status", e);
            return false;
        }
    }


    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setBusinessUserId(rs.getInt("business_user_id"));
        loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
        loan.setPurpose(rs.getString("purpose"));
        loan.setStatus(rs.getString("status"));
        loan.setRepaymentAmount(rs.getBigDecimal("repayment_amount"));
        loan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return loan;
    }
}
