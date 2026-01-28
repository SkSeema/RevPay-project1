package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

public class InvoiceItem {

    private static final Logger logger = LogManager.getLogger(InvoiceItem.class);

    private int itemId;
    private int invoiceId;
    private String itemName;
    private int quantity;
    private BigDecimal price;

    public InvoiceItem() {
        logger.debug("InvoiceItem object created");
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Alias methods for description (maps to itemName)
    public String getDescription() {
        return itemName;
    }

    public void setDescription(String description) {
        this.itemName = description;
    }
}
