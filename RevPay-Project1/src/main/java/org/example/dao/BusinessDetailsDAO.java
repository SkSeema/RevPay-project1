package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.BusinessDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class BusinessDetailsDAO {

    private static final Logger logger = LogManager.getLogger(BusinessDetailsDAO.class);


    public boolean insertBusinessDetails(BusinessDetails businessDetails) {
        String sql = "INSERT INTO business_details (user_id, business_name, business_type, tax_id, address, verification_doc, verified_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessDetails.getUserId());
            ps.setString(2, businessDetails.getBusinessName());
            ps.setString(3, businessDetails.getBusinessType());
            ps.setString(4, businessDetails.getTaxId());
            ps.setString(5, businessDetails.getAddress());
            ps.setString(6, businessDetails.getVerificationDoc());
            ps.setBoolean(7, businessDetails.isVerifiedStatus());
            int rows = ps.executeUpdate();
            logger.info("Business details inserted for userId={}, rows={}", businessDetails.getUserId(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error inserting business details for userId={}", businessDetails.getUserId(), e);
            return false;
        }
    }


    public BusinessDetails getBusinessDetailsByUserId(int userId) {
        String sql = "SELECT * FROM business_details WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BusinessDetails bd = new BusinessDetails();
                bd.setBusinessId(rs.getInt("business_id"));
                bd.setUserId(rs.getInt("user_id"));
                bd.setBusinessName(rs.getString("business_name"));
                bd.setBusinessType(rs.getString("business_type"));
                bd.setTaxId(rs.getString("tax_id"));
                bd.setAddress(rs.getString("address"));
                bd.setVerificationDoc(rs.getString("verification_doc"));
                bd.setVerifiedStatus(rs.getBoolean("verified_status"));
                return bd;
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error fetching business details for userId={}", userId, e);
            return null;
        }
    }


    public boolean updateVerificationStatus(int userId, boolean status) {
        String sql = "UPDATE business_details SET verified_status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, status);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            logger.info("Verification status updated for userId={}, status={}, rows={}", userId, status, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating verification status for userId={}", userId, e);
            return false;
        }
    }
}
