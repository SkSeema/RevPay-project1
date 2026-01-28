package org.example.service;

import org.example.dao.MoneyRequestDAO;
import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.MoneyRequest;
import org.example.model.User;
import org.example.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoneyRequestServiceTest {

    @Mock
    private MoneyRequestDAO dao;

    @Mock
    private WalletDAO walletDAO;

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserDAO userDAO;

    private MoneyRequestService moneyRequestService;

    private MoneyRequest testRequest;
    private User testUser;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        moneyRequestService = new MoneyRequestService(dao, walletDAO, transactionDAO, notificationService, userDAO);
        testRequest = new MoneyRequest();
        testRequest.setRequestId(1);
        testRequest.setFromUserId(1);
        testRequest.setToUserId(2);
        testRequest.setAmount(new BigDecimal("500.00"));
        testRequest.setStatus("PENDING");
        testRequest.setNote("Test request");

        testUser = new User();
        testUser.setUserId(1);
        testUser.setFullName("Test User");

        fromWallet = new Wallet();
        fromWallet.setWalletId(1);
        fromWallet.setUserId(2);
        fromWallet.setBalance(new BigDecimal("1000.00"));

        toWallet = new Wallet();
        toWallet.setWalletId(2);
        toWallet.setUserId(1);
        toWallet.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void testSendRequest_Success() {
        // Arrange
        when(dao.createRequest(any(MoneyRequest.class))).thenReturn(true);
        when(userDAO.getUserById(1)).thenReturn(testUser);

        // Act
        boolean result = moneyRequestService.sendRequest(testRequest);

        // Assert
        assertTrue(result);
        verify(dao, times(1)).createRequest(testRequest);
        verify(notificationService, times(1)).notifyMoneyRequest(eq(2), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    void testSendRequest_Failure() {
        // Arrange
        when(dao.createRequest(any(MoneyRequest.class))).thenReturn(false);

        // Act
        boolean result = moneyRequestService.sendRequest(testRequest);

        // Assert
        assertFalse(result);
        verify(dao, times(1)).createRequest(testRequest);
        verify(notificationService, never()).notifyMoneyRequest(anyInt(), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    void testGetRequestsForUser() {
        // Arrange
        List<MoneyRequest> requests = new ArrayList<>();
        requests.add(testRequest);
        when(dao.getRequestsForUser(1)).thenReturn(requests);

        // Act
        List<MoneyRequest> result = moneyRequestService.getRequestsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(dao, times(1)).getRequestsForUser(1);
    }

    @Test
    void testUpdateRequestStatus_Accepted() {
        // Arrange
        when(dao.updateRequestStatus(1, "ACCEPTED")).thenReturn(true);
        when(dao.getRequestById(1)).thenReturn(testRequest);
        when(walletDAO.transfer(2, 1, new BigDecimal("500.00"))).thenReturn(true);
        when(walletDAO.getWalletByUserId(2)).thenReturn(fromWallet);
        when(walletDAO.getWalletByUserId(1)).thenReturn(toWallet);
        when(userDAO.getUserById(1)).thenReturn(testUser);
        when(userDAO.getUserById(2)).thenReturn(testUser);

        // Act
        boolean result = moneyRequestService.updateRequestStatus(1, "ACCEPTED");

        // Assert
        assertTrue(result);
        verify(dao, times(1)).updateRequestStatus(1, "ACCEPTED");
        verify(walletDAO, times(1)).transfer(2, 1, new BigDecimal("500.00"));
        verify(transactionDAO, times(1)).logTransaction(any());
    }

    @Test
    void testUpdateRequestStatus_Rejected() {
        // Arrange
        when(dao.updateRequestStatus(1, "REJECTED")).thenReturn(true);
        when(dao.getRequestById(1)).thenReturn(testRequest);
        when(userDAO.getUserById(2)).thenReturn(testUser);

        // Act
        boolean result = moneyRequestService.updateRequestStatus(1, "REJECTED");

        // Assert
        assertTrue(result);
        verify(dao, times(1)).updateRequestStatus(1, "REJECTED");
        verify(walletDAO, never()).transfer(anyInt(), anyInt(), any(BigDecimal.class));
        verify(notificationService, times(1)).notifyMoneyRequest(eq(1), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    void testUpdateRequestStatus_TransferFails() {
        // Arrange
        when(dao.updateRequestStatus(1, "ACCEPTED")).thenReturn(true);
        when(dao.getRequestById(1)).thenReturn(testRequest);
        when(walletDAO.transfer(2, 1, new BigDecimal("500.00"))).thenReturn(false);

        // Act
        boolean result = moneyRequestService.updateRequestStatus(1, "ACCEPTED");

        // Assert
        assertTrue(result); // Status update succeeded
        verify(walletDAO, times(1)).transfer(2, 1, new BigDecimal("500.00"));
        verify(transactionDAO, never()).logTransaction(any()); // Transaction not logged due to transfer failure
    }
}
