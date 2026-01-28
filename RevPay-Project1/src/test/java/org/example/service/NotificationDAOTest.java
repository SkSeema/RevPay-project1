package org.example.service;

import org.example.dao.NotificationDAO;

import org.example.config.DBConnection;
import org.example.model.Notification;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationDAOTest {

    private NotificationDAO notificationDAO;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private MockedStatic<DBConnection> dbMock;

    @BeforeEach
    void setUp() throws Exception {
        notificationDAO = new NotificationDAO();

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

    // ✅ createNotification
    @Test
    void testCreateNotification_Success() throws Exception {
        when(connection.prepareStatement(anyString(), anyInt()))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        Notification n = new Notification();
        n.setUserId(1);
        n.setType("INFO");
        n.setMessage("Test notification");
        n.setRead(false);

        int id = notificationDAO.createNotification(n);

        assertEquals(10, id);
    }

    // ❌ createNotification failure
    @Test
    void testCreateNotification_Failure() throws Exception {
        when(connection.prepareStatement(anyString(), anyInt()))
                .thenThrow(SQLException.class);

        Notification n = new Notification();
        int id = notificationDAO.createNotification(n);

        assertEquals(0, id);
    }

    // ✅ getUnreadNotifications
    @Test
    void testGetUnreadNotifications() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("notification_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getString("type")).thenReturn("ALERT");
        when(resultSet.getString("message")).thenReturn("Low balance");
        when(resultSet.getBoolean("is_read")).thenReturn(false);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        List<Notification> list = notificationDAO.getUnreadNotifications(1);

        assertEquals(1, list.size());
        assertEquals("ALERT", list.get(0).getType());
    }

    // ✅ getUnreadCount
    @Test
    void testGetUnreadCount() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        int count = notificationDAO.getUnreadCount(1);

        assertEquals(3, count);
    }

    // ✅ markAsRead
    @Test
    void testMarkAsRead() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = notificationDAO.markAsRead(5);

        assertTrue(result);
    }

    // ❌ markAsRead failure
    @Test
    void testMarkAsRead_Failure() throws Exception {
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);

        boolean result = notificationDAO.markAsRead(5);

        assertFalse(result);
    }

    // ✅ markAllAsRead
    @Test
    void testMarkAllAsRead() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(2);

        boolean result = notificationDAO.markAllAsRead(1);

        assertTrue(result);
    }

    // ✅ getAllNotifications
    @Test
    void testGetAllNotifications() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("notification_id")).thenReturn(1, 2);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getString("type")).thenReturn("INFO");
        when(resultSet.getString("message")).thenReturn("Msg");
        when(resultSet.getBoolean("is_read")).thenReturn(true);
        when(resultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        List<Notification> list = notificationDAO.getAllNotifications(1);

        assertEquals(2, list.size());
    }
}

