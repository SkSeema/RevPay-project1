package org.example.service;

import org.example.dao.InvoiceDAO;

import org.example.config.DBConnection;
import org.example.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceDAOTest {

    private InvoiceDAO invoiceDAO;

    @BeforeEach
    void setUp() {
        invoiceDAO = new InvoiceDAO();
    }

    @Test
    void testCreateInvoice_success() throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setBusinessUserId(5);
        invoice.setCustomerIdentifier("customer@test.com");
        invoice.setTotalAmount(new BigDecimal("1000.00"));
        invoice.setStatus("UNPAID");
        invoice.setDueDate(LocalDate.of(2026, 1, 29));

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

            int generatedId = invoiceDAO.createInvoice(invoice);

            assertEquals(101, generatedId);
            verify(mockPs).setInt(1, invoice.getBusinessUserId());
            verify(mockPs).setString(2, invoice.getCustomerIdentifier());
            verify(mockPs).setBigDecimal(3, invoice.getTotalAmount());
            verify(mockPs).setString(4, invoice.getStatus());
            verify(mockPs).setDate(5, Date.valueOf(invoice.getDueDate()));
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetInvoicesByBusinessUserId_returnsInvoices() throws SQLException {
        int businessUserId = 5;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        // Mock two invoices in result set
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("invoice_id")).thenReturn(1, 2);
        when(mockRs.getInt("business_user_id")).thenReturn(businessUserId, businessUserId);
        when(mockRs.getString("customer_identifier")).thenReturn("c1@test.com", "c2@test.com");
        when(mockRs.getBigDecimal("total_amount")).thenReturn(new BigDecimal("1000.00"), new BigDecimal("2000.00"));
        when(mockRs.getString("status")).thenReturn("UNPAID", "PAID");
        when(mockRs.getDate("due_date")).thenReturn(Date.valueOf("2026-01-29"), Date.valueOf("2026-02-10"));
        when(mockRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<Invoice> invoices = invoiceDAO.getInvoicesByBusinessUserId(businessUserId);

            assertEquals(2, invoices.size());
            assertEquals(1, invoices.get(0).getInvoiceId());
            assertEquals(2, invoices.get(1).getInvoiceId());
        }
    }

    @Test
    void testGetInvoiceById_found() throws SQLException {
        int invoiceId = 1;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("invoice_id")).thenReturn(invoiceId);
        when(mockRs.getInt("business_user_id")).thenReturn(5);
        when(mockRs.getString("customer_identifier")).thenReturn("c@test.com");
        when(mockRs.getBigDecimal("total_amount")).thenReturn(new BigDecimal("1500.00"));
        when(mockRs.getString("status")).thenReturn("PAID");
        when(mockRs.getDate("due_date")).thenReturn(Date.valueOf("2026-01-30"));
        when(mockRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);

            assertNotNull(invoice);
            assertEquals(invoiceId, invoice.getInvoiceId());
        }
    }

    @Test
    void testUpdateInvoiceStatus_success() throws SQLException {
        int invoiceId = 1;
        String status = "PAID";

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = invoiceDAO.updateInvoiceStatus(invoiceId, status);

            assertTrue(result);
            verify(mockPs).setString(1, status);
            verify(mockPs).setInt(2, invoiceId);
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetUnpaidInvoices_returnsInvoices() throws SQLException {
        int businessUserId = 5;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("invoice_id")).thenReturn(1);
        when(mockRs.getInt("business_user_id")).thenReturn(businessUserId);
        when(mockRs.getString("customer_identifier")).thenReturn("c@test.com");
        when(mockRs.getBigDecimal("total_amount")).thenReturn(new BigDecimal("1000.00"));
        when(mockRs.getString("status")).thenReturn("UNPAID");
        when(mockRs.getDate("due_date")).thenReturn(Date.valueOf("2026-01-29"));
        when(mockRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<Invoice> invoices = invoiceDAO.getUnpaidInvoices(businessUserId);

            assertEquals(1, invoices.size());
            assertEquals("UNPAID", invoices.get(0).getStatus());
        }
    }
}
