package org.example.service;
import org.example.dao.BusinessDetailsDAO;
import org.example.config.DBConnection;
import org.example.model.BusinessDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessDetailsDAOTest {

    private BusinessDetailsDAO dao;

    @BeforeEach
    void setUp() {
        dao = new BusinessDetailsDAO();
    }

    @Test
    void testInsertBusinessDetails_success() throws SQLException {
        BusinessDetails bd = new BusinessDetails();
        bd.setUserId(1);
        bd.setBusinessName("MyBiz");
        bd.setBusinessType("Service");
        bd.setTaxId("TX123");
        bd.setAddress("Hyderabad");
        bd.setVerificationDoc("doc.pdf");
        bd.setVerifiedStatus(false);

        // Mock JDBC objects
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        // Mock static DBConnection.getInstance()
        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.insertBusinessDetails(bd);

            assertTrue(result);
            verify(mockPs).setInt(1, bd.getUserId());
            verify(mockPs).setString(2, bd.getBusinessName());
            verify(mockPs).setString(3, bd.getBusinessType());
            verify(mockPs).setString(4, bd.getTaxId());
            verify(mockPs).setString(5, bd.getAddress());
            verify(mockPs).setString(6, bd.getVerificationDoc());
            verify(mockPs).setBoolean(7, bd.isVerifiedStatus());
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetBusinessDetailsByUserId_found() throws SQLException {
        int userId = 1;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("business_id")).thenReturn(10);
        when(mockRs.getInt("user_id")).thenReturn(userId);
        when(mockRs.getString("business_name")).thenReturn("MyBiz");
        when(mockRs.getString("business_type")).thenReturn("Service");
        when(mockRs.getString("tax_id")).thenReturn("TX123");
        when(mockRs.getString("address")).thenReturn("Hyderabad");
        when(mockRs.getString("verification_doc")).thenReturn("doc.pdf");
        when(mockRs.getBoolean("verified_status")).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            BusinessDetails bd = dao.getBusinessDetailsByUserId(userId);

            assertNotNull(bd);
            assertEquals(10, bd.getBusinessId());
            assertEquals("MyBiz", bd.getBusinessName());
            assertFalse(bd.isVerifiedStatus());
        }
    }

    @Test
    void testUpdateVerificationStatus_success() throws SQLException {
        int userId = 1;
        boolean status = true;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.updateVerificationStatus(userId, status);

            assertTrue(result);
            verify(mockPs).setBoolean(1, status);
            verify(mockPs).setInt(2, userId);
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetBusinessDetailsByUserId_notFound() throws SQLException {
        int userId = 2;

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            BusinessDetails bd = dao.getBusinessDetailsByUserId(userId);

            assertNull(bd);
        }
    }
}
