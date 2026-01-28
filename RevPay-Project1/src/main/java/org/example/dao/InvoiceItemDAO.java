package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.InvoiceItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceItemDAO {

    private static final Logger logger = LogManager.getLogger(InvoiceItemDAO.class);


    public boolean addInvoiceItem(InvoiceItem item) {
        String sql = "INSERT INTO invoice_items (invoice_id, item_name, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getInvoiceId());
            ps.setString(2, item.getItemName());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());
            int rows = ps.executeUpdate();
            logger.info("Invoice item added: invoiceId={}, rows={}", item.getInvoiceId(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error adding invoice item", e);
            return false;
        }
    }


    public List<InvoiceItem> getItemsByInvoiceId(int invoiceId) {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ?";
        List<InvoiceItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InvoiceItem item = new InvoiceItem();
                item.setItemId(rs.getInt("item_id"));
                item.setInvoiceId(rs.getInt("invoice_id"));
                item.setItemName(rs.getString("item_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching invoice items for invoiceId={}", invoiceId, e);
        }
        return items;
    }
}
