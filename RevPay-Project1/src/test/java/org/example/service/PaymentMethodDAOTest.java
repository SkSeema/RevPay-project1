package org.example.service;

import org.example.dao.PaymentMethodDAO;

import org.example.config.DBConnection;
import org.example.model.PaymentMethod;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentMethodDAOTest {

    private PaymentMethodDAO paymentMethodDAO;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private MockedStatic<DBConnection> dbMock;

    @BeforeEach
    void setUp() throws Exception {
        paymentMethodDAO = new PaymentMethodDAO();

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

    // ✅ addPaymentMethod
    @Test
    void testAddPaymentMethod_Success() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        PaymentMethod pm = new PaymentMethod();
        pm.setUserId(1);
        pm.setMethodType("CARD");
        pm.setEncryptedDetails("enc-data");
        pm.setDefault(true);

        boolean result = paymentMethodDAO.addPaymentMethod(pm);

        assertTrue(result);
    }

    @Test
    void testAddPaymentMethod_Failure() throws Exception {
        when(connection.prepareStatement(anyString()))
                .thenThrow(SQLException.class);

        boolean result = paymentMethodDAO.addPaymentMethod(new PaymentMethod());

        assertFalse(result);
    }

    // ✅ getPaymentMethodsByUserId
    @Test
    void testGetPaymentMethodsByUserId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("payment_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getString("method_type")).thenReturn("UPI");
        when(resultSet.getString("encrypted_details")).thenReturn("enc");
        when(resultSet.getBoolean("is_default")).thenReturn(true);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        List<PaymentMethod> list = paymentMethodDAO.getPaymentMethodsByUserId(1);

        assertEquals(1, list.size());
        assertEquals("UPI", list.get(0).getMethodType());
    }

    // ✅ getPaymentMethodById
    @Test
    void testGetPaymentMethodById() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("payment_id")).thenReturn(2);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getString("method_type")).thenReturn("CARD");
        when(resultSet.getString("encrypted_details")).thenReturn("enc");
        when(resultSet.getBoolean("is_default")).thenReturn(false);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        PaymentMethod pm = paymentMethodDAO.getPaymentMethodById(2);

        assertNotNull(pm);
        assertEquals("CARD", pm.getMethodType());
    }

    // ✅ setDefaultPaymentMethod
    @Test
    void testSetDefaultPaymentMethod_Success() throws Exception {
        PreparedStatement unsetPs = mock(PreparedStatement.class);
        PreparedStatement setPs = mock(PreparedStatement.class);

        when(connection.prepareStatement(startsWith("UPDATE payment_methods SET is_default = 0")))
                .thenReturn(unsetPs);
        when(connection.prepareStatement(startsWith("UPDATE payment_methods SET is_default = 1")))
                .thenReturn(setPs);

        when(setPs.executeUpdate()).thenReturn(1);

        boolean result = paymentMethodDAO.setDefaultPaymentMethod(1, 10);

        assertTrue(result);
        verify(connection).commit();
    }

    @Test
    void testSetDefaultPaymentMethod_Failure() throws Exception {
        PreparedStatement unsetPs = mock(PreparedStatement.class);
        PreparedStatement setPs = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(unsetPs, setPs);
        when(setPs.executeUpdate()).thenReturn(0);

        boolean result = paymentMethodDAO.setDefaultPaymentMethod(1, 10);

        assertFalse(result);
        verify(connection).rollback();
    }

    // ✅ deletePaymentMethod
    @Test
    void testDeletePaymentMethod() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = paymentMethodDAO.deletePaymentMethod(5, 1);

        assertTrue(result);
    }

    // ✅ getDefaultPaymentMethod
    @Test
    void testGetDefaultPaymentMethod() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("payment_id")).thenReturn(3);
        when(resultSet.getString("method_type")).thenReturn("UPI");
        when(resultSet.getBoolean("is_default")).thenReturn(true);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        PaymentMethod pm = paymentMethodDAO.getDefaultPaymentMethod(1);

        assertNotNull(pm);
        assertTrue(pm.isDefault());
    }
}

