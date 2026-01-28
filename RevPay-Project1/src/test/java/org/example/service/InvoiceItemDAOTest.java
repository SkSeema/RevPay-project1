package org.example.service;

import org.example.config.DBConnection;
import org.example.dao.InvoiceItemDAO;
import org.example.model.InvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceItemDAOTest {

    private InvoiceItemDAO dao;

    @BeforeEach
    void setUp() {
        dao = new InvoiceItemDAO();
    }

    @Test
    void testAddInvoiceItem_success() throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setInvoiceId(1);
        item.setItemName("Product A");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("500.00"));

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.addInvoiceItem(item);

            assertTrue(result);
            verify(mockPs).setInt(1, item.getInvoiceId());
            verify(mockPs).setString(2, item.getItemName());
            verify(mockPs).setInt(3, item.getQuantity());
            verify(mockPs).setBigDecimal(4, item.getPrice());
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetItemsByInvoiceId_returnsItems() throws SQLException {
        int invoiceId = 1;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        // Mock two invoice items in the result set
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("item_id")).thenReturn(101, 102);
        when(mockRs.getInt("invoice_id")).thenReturn(invoiceId, invoiceId);
        when(mockRs.getString("item_name")).thenReturn("Product A", "Product B");
        when(mockRs.getInt("quantity")).thenReturn(2, 5);
        when(mockRs.getBigDecimal("price")).thenReturn(new BigDecimal("500.00"), new BigDecimal("200.00"));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<InvoiceItem> items = dao.getItemsByInvoiceId(invoiceId);

            assertEquals(2, items.size());
            assertEquals(101, items.get(0).getItemId());
            assertEquals("Product A", items.get(0).getItemName());
            assertEquals(5, items.get(1).getQuantity());
        }
    }

    @Test
    void testGetItemsByInvoiceId_empty() throws SQLException {
        int invoiceId = 99;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<InvoiceItem> items = dao.getItemsByInvoiceId(invoiceId);

            assertTrue(items.isEmpty());
        }
    }
}
