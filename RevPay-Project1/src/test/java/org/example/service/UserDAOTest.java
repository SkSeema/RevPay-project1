package org.example.service;

import org.example.dao.UserDAO;

import org.example.config.DBConnection;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDAOTest {

    private UserDAO dao;

    @BeforeEach
    void setUp() {
        dao = new UserDAO();
    }

    @Test
    void testInsertUser_success() throws SQLException {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPhone("9999999999");
        user.setPasswordHash("passHash");
        user.setTransactionPinHash("pinHash");
        user.setFullName("Test User");
        user.setAccountType("PERSONAL");
        user.setSecurityQuestion("Pet name?");
        user.setSecurityAnswerHash("answerHash");

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);
        when(mockPs.getGeneratedKeys()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            int userId = dao.insertUser(user);

            assertEquals(1, userId);
            verify(mockPs).setString(1, user.getEmail());
            verify(mockPs).setString(2, user.getPhone());
            verify(mockPs).setString(3, user.getPasswordHash());
            verify(mockPs).setString(4, user.getTransactionPinHash());
            verify(mockPs).setString(5, user.getFullName());
            verify(mockPs).setString(6, "PERSONAL");
            verify(mockPs).setString(7, user.getSecurityQuestion());
            verify(mockPs).setString(8, user.getSecurityAnswerHash());
            verify(mockPs).executeUpdate();
        }
    }

    @Test
    void testGetUserByEmailOrPhone_found() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);

        when(mockRs.getInt("user_id")).thenReturn(1);
        when(mockRs.getString("email")).thenReturn("test@gmail.com");
        when(mockRs.getString("phone")).thenReturn("9999999999");
        when(mockRs.getString("full_name")).thenReturn("Test User");
        when(mockRs.getString("account_type")).thenReturn("PERSONAL");
        when(mockRs.getString("password_hash")).thenReturn("passHash");
        when(mockRs.getString("transaction_pin_hash")).thenReturn("pinHash");
        when(mockRs.getString("security_question")).thenReturn("Pet?");
        when(mockRs.getString("security_answer_hash")).thenReturn("ansHash");
        when(mockRs.getInt("failed_attempts")).thenReturn(0);
        when(mockRs.getBoolean("account_locked")).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            User user = dao.getUserByEmailOrPhone("test@gmail.com");

            assertNotNull(user);
            assertEquals(1, user.getUserId());
            assertEquals("Test User", user.getFullName());
        }
    }

    @Test
    void testGetUserByEmailOrPhone_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            User user = dao.getUserByEmailOrPhone("unknown");

            assertNull(user);
        }
    }

    @Test
    void testUpdatePassword_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            boolean result = dao.updatePassword(1, "newHash");

            assertTrue(result);
            verify(mockPs).setString(1, "newHash");
            verify(mockPs).setInt(2, 1);
        }
    }

    @Test
    void testIncrementFailedAttempts_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            assertTrue(dao.incrementFailedAttempts(1));
        }
    }

    @Test
    void testLockAccount_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            assertTrue(dao.lockAccount(1));
        }
    }

    @Test
    void testResetFailedAttempts_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            assertTrue(dao.resetFailedAttempts(1));
        }
    }

    @Test
    void testUnlockAccount_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedStatic = mockStatic(DBConnection.class)) {
            mockedStatic.when(DBConnection::getInstance).thenReturn(mockConn);

            assertTrue(dao.unlockAccount(1));
        }
    }
}
