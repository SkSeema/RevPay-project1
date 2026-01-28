package org.example.service;

import org.example.dao.WalletDAO;

import org.example.config.DBConnection;
import org.example.model.Wallet;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletDAOTest {

    private WalletDAO walletDAO;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private MockedStatic<DBConnection> dbMock;

    @BeforeEach
    void setUp() throws Exception {
        walletDAO = new WalletDAO();

        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        dbMock = mockStatic(DBConnection.class);
        dbMock.when(DBConnection::getInstance).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }


    @Test
    void testCreateWallet_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = walletDAO.createWallet(1);

        assertTrue(result);
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setBigDecimal(2, BigDecimal.ZERO);
    }


    @Test
    void testGetWalletByUserId_Found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("wallet_id")).thenReturn(10);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getBigDecimal("balance")).thenReturn(BigDecimal.valueOf(500));
        when(resultSet.getString("currency")).thenReturn("INR");
        when(resultSet.getTimestamp("last_updated"))
                .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));

        Wallet wallet = walletDAO.getWalletByUserId(1);

        assertNotNull(wallet);
        assertEquals(1, wallet.getUserId());
        assertEquals(BigDecimal.valueOf(500), wallet.getBalance());
    }


    @Test
    void testUpdateBalance_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = walletDAO.updateBalance(1, BigDecimal.valueOf(1000));

        assertTrue(result);
    }


    @Test
    void testGetUserIdByIdentifier_Found() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user_id")).thenReturn(5);

        int userId = walletDAO.getUserIdByIdentifier("test@mail.com");

        assertEquals(5, userId);
    }


    @Test
    void testGetUserIdByIdentifier_NotFound() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int userId = walletDAO.getUserIdByIdentifier("unknown");

        assertEquals(-1, userId);
    }
}

