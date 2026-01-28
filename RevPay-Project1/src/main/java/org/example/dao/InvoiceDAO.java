package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.Invoice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    private static final Logger logger = LogManager.getLogger(InvoiceDAO.class);


    public int createInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (business_user_id, customer_identifier, total_amount, status, due_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getBusinessUserId());
            ps.setString(2, invoice.getCustomerIdentifier());
            ps.setBigDecimal(3, invoice.getTotalAmount());
            ps.setString(4, invoice.getStatus());
            ps.setDate(5, Date.valueOf(invoice.getDueDate()));
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int invoiceId = rs.getInt(1);
                    logger.info("Invoice created: invoiceId={}", invoiceId);
                    return invoiceId;
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error creating invoice", e);
            return 0;
        }
    }


    public List<Invoice> getInvoicesByBusinessUserId(int businessUserId) {
        String sql = "SELECT * FROM invoices WHERE business_user_id = ? ORDER BY created_at DESC";
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoices.add(invoice);
            }
            logger.info("Retrieved {} invoices for businessUserId={}", invoices.size(), businessUserId);
        } catch (SQLException e) {
            logger.error("Error fetching invoices", e);
        }
        return invoices;
    }


    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToInvoice(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching invoice by ID={}", invoiceId, e);
        }
        return null;
    }


    public boolean updateInvoiceStatus(int invoiceId, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, invoiceId);
            int rows = ps.executeUpdate();
            logger.info("Invoice status updated: invoiceId={}, status={}, rows={}", invoiceId, status, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating invoice status", e);
            return false;
        }
    }


    public List<Invoice> getUnpaidInvoices(int businessUserId) {
        String sql = "SELECT * FROM invoices WHERE business_user_id = ? AND status = 'UNPAID' ORDER BY due_date ASC";
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            logger.error("Error fetching unpaid invoices", e);
        }
        return invoices;
    }


    public List<Invoice> getPaidInvoices(int businessUserId) {
        String sql = "SELECT * FROM invoices WHERE business_user_id = ? AND status = 'PAID' ORDER BY created_at DESC";
        List<Invoice> invoices = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invoice invoice = mapResultSetToInvoice(rs);
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            logger.error("Error fetching paid invoices", e);
        }
        return invoices;
    }


    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setBusinessUserId(rs.getInt("business_user_id"));
        invoice.setCustomerIdentifier(rs.getString("customer_identifier"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setStatus(rs.getString("status"));
        invoice.setDueDate(rs.getDate("due_date").toLocalDate());
        invoice.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return invoice;
    }
}
