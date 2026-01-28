package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.Transaction;
import org.example.model.User;
import org.example.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMoneyServiceTest {

    @Mock
    private WalletDAO walletDAO;

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserDAO userDAO;

    private SendMoneyService sendMoneyService;

    private Wallet senderWallet;
    private Wallet recipientWallet;
    private User senderUser;
    private User recipientUser;

    @BeforeEach
    void setUp() {
        sendMoneyService = new SendMoneyService(walletDAO, transactionDAO, notificationService, userDAO);
        senderWallet = new Wallet();
        senderWallet.setWalletId(1);
        senderWallet.setUserId(1);
        senderWallet.setBalance(new BigDecimal("1000.00"));

        recipientWallet = new Wallet();
        recipientWallet.setWalletId(2);
        recipientWallet.setUserId(2);
        recipientWallet.setBalance(new BigDecimal("500.00"));

        senderUser = new User();
        senderUser.setUserId(1);
        senderUser.setFullName("Sender User");

        recipientUser = new User();
        recipientUser.setUserId(2);
        recipientUser.setFullName("Recipient User");
    }

    @Test
    void testSendMoney_Success() {
        // Arrange
        when(walletDAO.getUserIdByIdentifier("recipient@example.com")).thenReturn(2);
        when(walletDAO.getWalletByUserId(1)).thenReturn(senderWallet);
        when(walletDAO.getWalletByUserId(2)).thenReturn(recipientWallet);
        when(walletDAO.transfer(1, 2, new BigDecimal("200.00"))).thenReturn(true);
        when(userDAO.getUserById(1)).thenReturn(senderUser);
        when(userDAO.getUserById(2)).thenReturn(recipientUser);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "recipient@example.com", new BigDecimal("200.00"), "Test payment");

        // Assert
        assertTrue(result);
        verify(walletDAO, times(1)).transfer(1, 2, new BigDecimal("200.00"));
        verify(transactionDAO, times(1)).logTransaction(any(Transaction.class));
        verify(notificationService, times(2)).notifyTransaction(anyInt(), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    void testSendMoney_RecipientNotFound() {
        // Arrange
        when(walletDAO.getUserIdByIdentifier("unknown@example.com")).thenReturn(-1);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "unknown@example.com", new BigDecimal("200.00"), "Test");

        // Assert
        assertFalse(result);
        verify(walletDAO, never()).transfer(anyInt(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testSendMoney_InsufficientBalance() {
        // Arrange
        when(walletDAO.getUserIdByIdentifier("recipient@example.com")).thenReturn(2);
        when(walletDAO.getWalletByUserId(1)).thenReturn(senderWallet);
        when(walletDAO.getWalletByUserId(2)).thenReturn(recipientWallet);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "recipient@example.com", new BigDecimal("2000.00"), "Test");

        // Assert
        assertFalse(result);
        verify(walletDAO, never()).transfer(anyInt(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testSendMoney_SenderWalletNotFound() {
        // Arrange
        when(walletDAO.getUserIdByIdentifier("recipient@example.com")).thenReturn(2);
        when(walletDAO.getWalletByUserId(1)).thenReturn(null);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "recipient@example.com", new BigDecimal("200.00"), "Test");

        // Assert
        assertFalse(result);
        verify(walletDAO, never()).transfer(anyInt(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testSendMoney_RecipientWalletNotFound() {
        // Arrange
        when(walletDAO.getUserIdByIdentifier("recipient@example.com")).thenReturn(2);
        when(walletDAO.getWalletByUserId(1)).thenReturn(senderWallet);
        when(walletDAO.getWalletByUserId(2)).thenReturn(null);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "recipient@example.com", new BigDecimal("200.00"), "Test");

        // Assert
        assertFalse(result);
        verify(walletDAO, never()).transfer(anyInt(), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testSendMoney_LowBalanceAlert() {
        // Arrange
        senderWallet.setBalance(new BigDecimal("600.00"));
        Wallet updatedWallet = new Wallet();
        updatedWallet.setBalance(new BigDecimal("400.00")); // Below threshold

        when(walletDAO.getUserIdByIdentifier("recipient@example.com")).thenReturn(2);
        when(walletDAO.getWalletByUserId(1)).thenReturn(senderWallet, updatedWallet);
        when(walletDAO.getWalletByUserId(2)).thenReturn(recipientWallet);
        when(walletDAO.transfer(1, 2, new BigDecimal("200.00"))).thenReturn(true);
        when(userDAO.getUserById(1)).thenReturn(senderUser);
        when(userDAO.getUserById(2)).thenReturn(recipientUser);

        // Act
        boolean result = sendMoneyService.sendMoney(1, "recipient@example.com", new BigDecimal("200.00"), "Test");

        // Assert
        assertTrue(result);
        verify(notificationService, times(1)).notifyLowBalance(eq(1), any(BigDecimal.class));
    }
}
