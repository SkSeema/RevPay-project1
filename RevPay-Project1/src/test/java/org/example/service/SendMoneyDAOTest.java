package org.example.service;

import org.example.dao.SendMoneyDAO;

import org.example.config.DBConnection;
import org.example.model.SendMoney;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendMoneyDAOTest {

    @Test
    void testCreateTransaction_success() throws Exception {

        // mocks
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<DBConnection> mockedDB = mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getInstance).thenReturn(conn);
            when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            when(ps.getGeneratedKeys()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(101);

            SendMoney transaction = new SendMoney();
            transaction.setSenderUserId(1);
            transaction.setReceiverUserId(2);
            transaction.setAmount(new BigDecimal("500.00"));
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setStatus("SUCCESS");

            SendMoneyDAO dao = new SendMoneyDAO();
            boolean result = dao.createTransaction(transaction);

            assertTrue(result);
            assertEquals(101, transaction.getTransactionId());

            verify(ps).executeUpdate();
        }
    }

    @Test
    void testGetTransactionById_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<DBConnection> mockedDB = mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getInstance).thenReturn(conn);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt("transaction_id")).thenReturn(10);
            when(rs.getInt("sender_user_id")).thenReturn(1);
            when(rs.getInt("receiver_user_id")).thenReturn(2);
            when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("250.00"));
            when(rs.getTimestamp("timestamp"))
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            when(rs.getString("status")).thenReturn("SUCCESS");

            SendMoneyDAO dao = new SendMoneyDAO();
            SendMoney tx = dao.getTransactionById(10);

            assertNotNull(tx);
            assertEquals(10, tx.getTransactionId());
            assertEquals("SUCCESS", tx.getStatus());
        }
    }
}
