package org.example.dao;

import org.example.config.DBConnection;
import org.example.model.MoneyRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoneyRequestDAO {


    public boolean createRequest(MoneyRequest request) {
        String sql = "INSERT INTO money_requests (from_user_id, to_user_id, amount, status, note, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, request.getFromUserId());
            ps.setInt(2, request.getToUserId());
            ps.setBigDecimal(3, request.getAmount());
            ps.setString(4, request.getStatus() != null ? request.getStatus() : "PENDING");
            ps.setString(5, request.getNote());
            ps.setTimestamp(6, Timestamp.valueOf(request.getCreatedAt() != null ? request.getCreatedAt() : java.time.LocalDateTime.now()));
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<MoneyRequest> getRequestsForUser(int userId) {
        String sql = "SELECT request_id, from_user_id, to_user_id, amount, status, note, created_at FROM money_requests WHERE to_user_id = ? ORDER BY created_at DESC";
        List<MoneyRequest> list = new ArrayList<>();
        try (Connection con = DBConnection.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MoneyRequest r = new MoneyRequest();
                r.setRequestId(rs.getInt("request_id"));
                r.setFromUserId(rs.getInt("from_user_id"));
                r.setToUserId(rs.getInt("to_user_id"));
                r.setAmount(rs.getBigDecimal("amount"));
                r.setStatus(rs.getString("status"));
                r.setNote(rs.getString("note"));
                r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // Update request status
    public boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE money_requests SET status = ? WHERE request_id = ?";
        try (Connection con = DBConnection.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get request by ID
    public MoneyRequest getRequestById(int requestId) {
        String sql = "SELECT request_id, from_user_id, to_user_id, amount, status, note, created_at FROM money_requests WHERE request_id = ?";
        try (Connection con = DBConnection.getInstance();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MoneyRequest r = new MoneyRequest();
                r.setRequestId(rs.getInt("request_id"));
                r.setFromUserId(rs.getInt("from_user_id"));
                r.setToUserId(rs.getInt("to_user_id"));
                r.setAmount(rs.getBigDecimal("amount"));
                r.setStatus(rs.getString("status"));
                r.setNote(rs.getString("note"));
                r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return r;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
