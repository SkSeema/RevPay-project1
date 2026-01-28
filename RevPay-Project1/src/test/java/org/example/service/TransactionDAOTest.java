package org.example.service;

import org.example.dao.TransactionDAO;

import org.example.config.DBConnection;
import org.example.model.Transaction;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionDAOTest {

    private TransactionDAO transactionDAO;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private MockedStatic<DBConnection> dbMock;

    @BeforeEach
    void setUp() throws Exception {
        transactionDAO = new TransactionDAO();

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

    // ✅ logTransaction
    @Test
    void testLogTransaction_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Transaction tx = new Transaction();
        tx.setFromWalletId(1);
        tx.setToWalletId(2);
        tx.setAmount(BigDecimal.valueOf(500));
        tx.setTransactionType("TRANSFER");
        tx.setStatus("SUCCESS");
        tx.setNote("Test transfer");
        tx.setCreatedAt(LocalDateTime.now());

        boolean result = transactionDAO.logTransaction(tx);

        assertTrue(result);
        verify(preparedStatement).executeUpdate();
    }

    // ❌ logTransaction failure
    @Test
    void testLogTransaction_Failure() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        Transaction tx = new Transaction();
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setTransactionType("CREDIT");

        boolean result = transactionDAO.logTransaction(tx);

        assertFalse(result);
    }

    // ✅ getTransactionsByWalletId
    @Test
    void testGetTransactionsByWalletId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("transaction_id")).thenReturn(1);
        when(resultSet.getInt("from_wallet_id")).thenReturn(10);
        when(resultSet.getInt("to_wallet_id")).thenReturn(20);
        when(resultSet.getBigDecimal("amount")).thenReturn(BigDecimal.valueOf(300));
        when(resultSet.getString("transaction_type")).thenReturn("DEBIT");
        when(resultSet.getString("status")).thenReturn("SUCCESS");
        when(resultSet.getString("note")).thenReturn("Shopping");
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));

        List<Transaction> list = transactionDAO.getTransactionsByWalletId(10);

        assertEquals(1, list.size());
        assertEquals(BigDecimal.valueOf(300), list.get(0).getAmount());
    }

    // ✅ getTransactionsByUserId
    @Test
    void testGetTransactionsByUserId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("transaction_id")).thenReturn(1, 2);
        when(resultSet.getInt("from_wallet_id")).thenReturn(5, 6);
        when(resultSet.getInt("to_wallet_id")).thenReturn(7, 8);
        when(resultSet.getBigDecimal("amount")).thenReturn(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200)
        );
        when(resultSet.getString("transaction_type")).thenReturn("CREDIT");
        when(resultSet.getString("status")).thenReturn("SUCCESS");
        when(resultSet.getString("note")).thenReturn("Salary");
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf("2024-01-01 09:00:00"));

        List<Transaction> list = transactionDAO.getTransactionsByUserId(1);

        assertEquals(2, list.size());
    }
}

