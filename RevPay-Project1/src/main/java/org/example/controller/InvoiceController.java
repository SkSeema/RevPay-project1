package org.example.controller;

import org.example.model.Invoice;
import org.example.model.InvoiceItem;
import org.example.service.InvoiceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InvoiceController {

    private static final Logger logger = LogManager.getLogger(InvoiceController.class);
    private final InvoiceService invoiceService = new InvoiceService();
    private final Scanner scanner = new Scanner(System.in);


    public void manageInvoices(int businessUserId) {
        while (true) {
            logger.info("\n======= INVOICE MANAGEMENT =======");
            logger.info("1. Create New Invoice");
            logger.info("2. View All Invoices");
            logger.info("3. View Unpaid Invoices");
            logger.info("4. View Paid Invoices");
            logger.info("5. View Invoice Details");
            logger.info("6. Process Invoice Payment");
            logger.info("7. Back to Dashboard");
            logger.info("===================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> createInvoice(businessUserId);
                case "2" -> viewAllInvoices(businessUserId);
                case "3" -> viewUnpaidInvoices(businessUserId);
                case "4" -> viewPaidInvoices(businessUserId);
                case "5" -> viewInvoiceDetails();
                case "6" -> processPayment();
                case "7" -> {
                    logger.info("Returning to dashboard...");
                    return;
                }
                default -> logger.warn("Invalid option. Please try again.");
            }
        }
    }

    private void createInvoice(int businessUserId) {
        logger.info("\n--- CREATE NEW INVOICE ---");

        System.out.print("Enter customer identifier (email/phone/username): ");
        String customerIdentifier = scanner.nextLine();

        List<InvoiceItem> items = new ArrayList<>();
        boolean addMoreItems = true;

        while (addMoreItems) {
            System.out.print("\nEnter item description: ");
            String description = scanner.nextLine();

            System.out.print("Enter quantity: ");
            int quantity;
            try {
                quantity = Integer.parseInt(scanner.nextLine());
                if (quantity <= 0) {
                    logger.warn("Quantity must be positive");
                    continue;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid quantity");
                continue;
            }

            System.out.print("Enter price per unit: ");
            BigDecimal price;
            try {
                price = new BigDecimal(scanner.nextLine());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.warn("Price must be positive");
                    continue;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid price");
                continue;
            }

            InvoiceItem item = new InvoiceItem();
            item.setDescription(description);
            item.setQuantity(quantity);
            item.setPrice(price);
            items.add(item);

            logger.info("Item added: {} x {} @ ₹{}", description, quantity, price);

            System.out.print("Add more items? (y/n): ");
            addMoreItems = scanner.nextLine().equalsIgnoreCase("y");
        }

        System.out.print("\nEnter due date (YYYY-MM-DD): ");
        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format. Using 30 days from now.");
            dueDate = LocalDate.now().plusDays(30);
        }

        int invoiceId = invoiceService.createInvoice(businessUserId, customerIdentifier, items, dueDate);
        if (invoiceId > 0) {
            logger.info("✓ Invoice created successfully! Invoice ID: {}", invoiceId);
        } else {
            logger.error("✗ Failed to create invoice");
        }
    }

    private void viewAllInvoices(int businessUserId) {
        List<Invoice> invoices = invoiceService.getBusinessInvoices(businessUserId);
        displayInvoiceList(invoices, "ALL INVOICES");
    }

    private void viewUnpaidInvoices(int businessUserId) {
        List<Invoice> invoices = invoiceService.getUnpaidInvoices(businessUserId);
        displayInvoiceList(invoices, "UNPAID INVOICES");
    }

    private void viewPaidInvoices(int businessUserId) {
        List<Invoice> invoices = invoiceService.getPaidInvoices(businessUserId);
        displayInvoiceList(invoices, "PAID INVOICES");
    }

    private void displayInvoiceList(List<Invoice> invoices, String title) {
        logger.info("\n--- {} ---", title);
        if (invoices.isEmpty()) {
            logger.info("No invoices found");
            return;
        }

        for (Invoice invoice : invoices) {
            logger.info("Invoice ID: {} | Customer: {} | Amount: ₹{} | Status: {} | Due: {}", 
                invoice.getInvoiceId(), 
                invoice.getCustomerIdentifier(), 
                invoice.getTotalAmount(), 
                invoice.getStatus(),
                invoice.getDueDate());
        }
        logger.info("Total: {} invoice(s)", invoices.size());
    }

    private void viewInvoiceDetails() {
        System.out.print("Enter invoice ID: ");
        int invoiceId;
        try {
            invoiceId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            logger.warn("Invalid invoice ID");
            return;
        }

        Invoice invoice = invoiceService.getInvoiceWithItems(invoiceId);
        if (invoice == null) {
            logger.warn("Invoice not found");
            return;
        }

        logger.info("\n========= INVOICE DETAILS =========");
        logger.info("Invoice ID: {}", invoice.getInvoiceId());
        logger.info("Customer: {}", invoice.getCustomerIdentifier());
        logger.info("Status: {}", invoice.getStatus());
        logger.info("Due Date: {}", invoice.getDueDate());
        logger.info("Created: {}", invoice.getCreatedAt());
        
        logger.info("\n--- ITEMS ---");
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (InvoiceItem item : invoice.getItems()) {
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                logger.info("{} x {} @ ₹{} = ₹{}", 
                    item.getDescription(), 
                    item.getQuantity(), 
                    item.getPrice(),
                    itemTotal);
            }
        }
        
        logger.info("\nTotal Amount: ₹{}", invoice.getTotalAmount());
        logger.info("===================================");
    }

    private void processPayment() {
        System.out.print("Enter invoice ID to process payment: ");
        int invoiceId;
        try {
            invoiceId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            logger.warn("Invalid invoice ID");
            return;
        }

        // Show invoice details first
        Invoice invoice = invoiceService.getInvoiceWithItems(invoiceId);
        if (invoice == null) {
            logger.warn("Invoice not found");
            return;
        }

        logger.info("Invoice Amount: ₹{} | Status: {}", invoice.getTotalAmount(), invoice.getStatus());

        System.out.print("Payment method (CASH/CARD/UPI): ");
        String paymentMethod = scanner.nextLine();

        boolean success = invoiceService.processInvoicePayment(invoiceId, paymentMethod);
        if (success) {
            logger.info("✓ Payment processed successfully!");
        } else {
            logger.error("✗ Failed to process payment");
        }
    }
}
