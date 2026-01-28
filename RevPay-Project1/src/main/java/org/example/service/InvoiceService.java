package org.example.service;

import org.example.dao.InvoiceDAO;
import org.example.dao.InvoiceItemDAO;
import org.example.dao.WalletDAO;
import org.example.model.Invoice;
import org.example.model.InvoiceItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceService {

    private static final Logger logger = LogManager.getLogger(InvoiceService.class);
    private final InvoiceDAO invoiceDAO;
    private final InvoiceItemDAO invoiceItemDAO;
    private final WalletDAO walletDAO;

    // Default constructor for production use
    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceItemDAO = new InvoiceItemDAO();
        this.walletDAO = new WalletDAO();
    }

    // Constructor for testing with dependency injection
    public InvoiceService(InvoiceDAO invoiceDAO, InvoiceItemDAO invoiceItemDAO, WalletDAO walletDAO) {
        this.invoiceDAO = invoiceDAO;
        this.invoiceItemDAO = invoiceItemDAO;
        this.walletDAO = walletDAO;
    }

    // Create invoice with items
    public int createInvoice(int businessUserId, String customerIdentifier, List<InvoiceItem> items, LocalDate dueDate) {
        // Calculate total amount
        BigDecimal totalAmount = items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setBusinessUserId(businessUserId);
        invoice.setCustomerIdentifier(customerIdentifier);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("UNPAID");
        invoice.setDueDate(dueDate);

        int invoiceId = invoiceDAO.createInvoice(invoice);
        
        if (invoiceId > 0) {
            // Add invoice items
            for (InvoiceItem item : items) {
                item.setInvoiceId(invoiceId);
                invoiceItemDAO.addInvoiceItem(item);
            }
            logger.info("Invoice created with {} items, totalAmount={}", items.size(), totalAmount);
        }
        
        return invoiceId;
    }

    // Get all invoices for business
    public List<Invoice> getBusinessInvoices(int businessUserId) {
        return invoiceDAO.getInvoicesByBusinessUserId(businessUserId);
    }

    // Get invoice details with items
    public Invoice getInvoiceWithItems(int invoiceId) {
        Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
        if (invoice != null) {
            List<InvoiceItem> items = invoiceItemDAO.getItemsByInvoiceId(invoiceId);
            invoice.setItems(items);
        }
        return invoice;
    }

    // Process payment for invoice
    public boolean processInvoicePayment(int invoiceId, String paymentMethod) {
        Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
        if (invoice == null) {
            logger.warn("Invoice not found: invoiceId={}", invoiceId);
            return false;
        }

        if ("PAID".equals(invoice.getStatus())) {
            logger.warn("Invoice already paid: invoiceId={}", invoiceId);
            return false;
        }

        // Mark as paid
        boolean updated = invoiceDAO.updateInvoiceStatus(invoiceId, "PAID");
        
        if (updated) {
            logger.info("Invoice payment processed: invoiceId={}, amount={}, method={}", 
                invoiceId, invoice.getTotalAmount(), paymentMethod);
        }
        
        return updated;
    }

    // Get unpaid invoices
    public List<Invoice> getUnpaidInvoices(int businessUserId) {
        return invoiceDAO.getUnpaidInvoices(businessUserId);
    }

    // Get paid invoices
    public List<Invoice> getPaidInvoices(int businessUserId) {
        return invoiceDAO.getPaidInvoices(businessUserId);
    }
}
