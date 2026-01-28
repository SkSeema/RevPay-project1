package org.example.service;

import org.example.dao.MoneyRequestDAO;

import org.example.config.DBConnection;
import org.example.model.MoneyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoneyRequestDAOTest {

    private MoneyRequestDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MoneyRequestDAO();
    }

    @Test
    void testCreateRequest_success() throws SQLException {
        MoneyRequest request = new MoneyRequest();
        request.setFromUserId(1);
        request.setToUserId(2);
        request.setAmount(new BigDecimal("500.00"));
        request.setNote("Lunch");
        request.setCreatedAt(LocalDateTime.now());

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.createRequest(request);

            assertTrue(result);
            verify(mockPs).setInt(1, 1);
            verify(mockPs).setInt(2, 2);
            verify(mockPs).setBigDecimal(3, new BigDecimal("500.00"));
            verify(mockPs).setString(4, "PENDING");
            verify(mockPs).setString(5, "Lunch");
            verify(mockPs).setTimestamp(eq(6), any(Timestamp.class));
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetRequestsForUser() throws SQLException {
        int userId = 2;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("request_id")).thenReturn(101);
        when(mockRs.getInt("from_user_id")).thenReturn(1);
        when(mockRs.getInt("to_user_id")).thenReturn(2);
        when(mockRs.getBigDecimal("amount")).thenReturn(new BigDecimal("500.00"));
        when(mockRs.getString("status")).thenReturn("PENDING");
        when(mockRs.getString("note")).thenReturn("Lunch");
        when(mockRs.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf("2026-01-27 10:00:00"));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            List<MoneyRequest> list = dao.getRequestsForUser(userId);

            assertEquals(1, list.size());
            assertEquals(101, list.get(0).getRequestId());
            assertEquals("PENDING", list.get(0).getStatus());
        }
    }

    @Test
    void testUpdateRequestStatus_success() throws SQLException {
        int requestId = 101;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.updateRequestStatus(requestId, "APPROVED");

            assertTrue(result);
            verify(mockPs).setString(1, "APPROVED");
            verify(mockPs).setInt(2, requestId);
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetRequestById_found() throws SQLException {
        int requestId = 101;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);

        when(mockRs.getInt("request_id")).thenReturn(101);
        when(mockRs.getInt("from_user_id")).thenReturn(1);
        when(mockRs.getInt("to_user_id")).thenReturn(2);
        when(mockRs.getBigDecimal("amount")).thenReturn(new BigDecimal("500.00"));
        when(mockRs.getString("status")).thenReturn("PENDING");
        when(mockRs.getString("note")).thenReturn("Lunch");
        when(mockRs.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf("2026-01-27 10:00:00"));

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            MoneyRequest request = dao.getRequestById(requestId);

            assertNotNull(request);
            assertEquals(101, request.getRequestId());
            assertEquals("Lunch", request.getNote());
        }
    }

    @Test
    void testGetRequestById_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            MoneyRequest request = dao.getRequestById(999);

            assertNull(request);
        }
    }
}
