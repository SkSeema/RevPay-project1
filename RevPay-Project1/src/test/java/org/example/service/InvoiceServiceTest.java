package org.example.service;

import org.example.dao.InvoiceDAO;
import org.example.dao.InvoiceItemDAO;
import org.example.dao.WalletDAO;
import org.example.model.Invoice;
import org.example.model.InvoiceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceDAO invoiceDAO;

    @Mock
    private InvoiceItemDAO invoiceItemDAO;

    @Mock
    private WalletDAO walletDAO;

    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private List<InvoiceItem> testItems;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceDAO, invoiceItemDAO, walletDAO);
        testInvoice = new Invoice();
        testInvoice.setInvoiceId(1);
        testInvoice.setBusinessUserId(1);
        testInvoice.setCustomerIdentifier("customer@example.com");
        testInvoice.setTotalAmount(new BigDecimal("1000.00"));
        testInvoice.setStatus("UNPAID");

        testItems = new ArrayList<>();
        InvoiceItem item1 = new InvoiceItem();
        item1.setItemName("Product 1");
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("500.00"));
        testItems.add(item1);
    }

    @Test
    void testCreateInvoice_Success() {
        // Arrange
        when(invoiceDAO.createInvoice(any(Invoice.class))).thenReturn(1);
        when(invoiceItemDAO.addInvoiceItem(any(InvoiceItem.class))).thenReturn(true);

        // Act
        int invoiceId = invoiceService.createInvoice(1, "customer@example.com", testItems, LocalDate.now().plusDays(30));

        // Assert
        assertEquals(1, invoiceId);
        verify(invoiceDAO, times(1)).createInvoice(any(Invoice.class));
        verify(invoiceItemDAO, times(1)).addInvoiceItem(any(InvoiceItem.class));
    }

    @Test
    void testGetBusinessInvoices() {
        // Arrange
        List<Invoice> invoices = new ArrayList<>();
        invoices.add(testInvoice);
        when(invoiceDAO.getInvoicesByBusinessUserId(1)).thenReturn(invoices);

        // Act
        List<Invoice> result = invoiceService.getBusinessInvoices(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceDAO, times(1)).getInvoicesByBusinessUserId(1);
    }

    @Test
    void testGetInvoiceWithItems() {
        // Arrange
        when(invoiceDAO.getInvoiceById(1)).thenReturn(testInvoice);
        when(invoiceItemDAO.getItemsByInvoiceId(1)).thenReturn(testItems);

        // Act
        Invoice result = invoiceService.getInvoiceWithItems(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        verify(invoiceDAO, times(1)).getInvoiceById(1);
        verify(invoiceItemDAO, times(1)).getItemsByInvoiceId(1);
    }

    @Test
    void testProcessInvoicePayment_Success() {
        // Arrange
        when(invoiceDAO.getInvoiceById(1)).thenReturn(testInvoice);
        when(invoiceDAO.updateInvoiceStatus(1, "PAID")).thenReturn(true);

        // Act
        boolean result = invoiceService.processInvoicePayment(1, "CASH");

        // Assert
        assertTrue(result);
        verify(invoiceDAO, times(1)).getInvoiceById(1);
        verify(invoiceDAO, times(1)).updateInvoiceStatus(1, "PAID");
    }

    @Test
    void testProcessInvoicePayment_AlreadyPaid() {
        // Arrange
        testInvoice.setStatus("PAID");
        when(invoiceDAO.getInvoiceById(1)).thenReturn(testInvoice);

        // Act
        boolean result = invoiceService.processInvoicePayment(1, "CASH");

        // Assert
        assertFalse(result);
        verify(invoiceDAO, times(1)).getInvoiceById(1);
        verify(invoiceDAO, never()).updateInvoiceStatus(anyInt(), anyString());
    }

    @Test
    void testProcessInvoicePayment_InvoiceNotFound() {
        // Arrange
        when(invoiceDAO.getInvoiceById(1)).thenReturn(null);

        // Act
        boolean result = invoiceService.processInvoicePayment(1, "CASH");

        // Assert
        assertFalse(result);
        verify(invoiceDAO, times(1)).getInvoiceById(1);
        verify(invoiceDAO, never()).updateInvoiceStatus(anyInt(), anyString());
    }

    @Test
    void testGetUnpaidInvoices() {
        // Arrange
        List<Invoice> invoices = new ArrayList<>();
        invoices.add(testInvoice);
        when(invoiceDAO.getUnpaidInvoices(1)).thenReturn(invoices);

        // Act
        List<Invoice> result = invoiceService.getUnpaidInvoices(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceDAO, times(1)).getUnpaidInvoices(1);
    }

    @Test
    void testGetPaidInvoices() {
        // Arrange
        List<Invoice> invoices = new ArrayList<>();
        testInvoice.setStatus("PAID");
        invoices.add(testInvoice);
        when(invoiceDAO.getPaidInvoices(1)).thenReturn(invoices);

        // Act
        List<Invoice> result = invoiceService.getPaidInvoices(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceDAO, times(1)).getPaidInvoices(1);
    }
}
