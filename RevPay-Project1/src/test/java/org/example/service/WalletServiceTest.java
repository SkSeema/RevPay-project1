package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.WalletDAO;
import org.example.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletDAO walletDAO;

    @Mock
    private TransactionDAO transactionDAO;

    private WalletService walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletDAO, transactionDAO);
        testWallet = new Wallet();
        testWallet.setWalletId(1);
        testWallet.setUserId(1);
        testWallet.setBalance(new BigDecimal("1000.00"));
        testWallet.setCurrency("INR");
    }

    @Test
    void testGetWallet() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);

        // Act
        Wallet result = walletService.getWallet(1);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(walletDAO, times(1)).getWalletByUserId(1);
    }

    @Test
    void testDeposit_Success() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("500.00");
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);
        when(walletDAO.updateBalance(eq(1), any(BigDecimal.class))).thenReturn(true);

        // Act
        boolean result = walletService.deposit(1, depositAmount);

        // Assert
        assertTrue(result);
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(walletDAO, times(1)).updateBalance(eq(1), any(BigDecimal.class));
    }

    @Test
    void testDeposit_NullWallet() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(null);

        // Act
        boolean result = walletService.deposit(1, new BigDecimal("500.00"));

        // Assert
        assertFalse(result);
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(walletDAO, never()).updateBalance(anyInt(), any(BigDecimal.class));
    }

    @Test
    void testWithdraw_Success() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("300.00");
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);
        when(walletDAO.updateBalance(eq(1), any(BigDecimal.class))).thenReturn(true);

        // Act
        boolean result = walletService.withdraw(1, withdrawAmount);

        // Assert
        assertTrue(result);
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(walletDAO, times(1)).updateBalance(eq(1), any(BigDecimal.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("1500.00");
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);

        // Act
        boolean result = walletService.withdraw(1, withdrawAmount);

        // Assert
        assertFalse(result);
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(walletDAO, never()).updateBalance(anyInt(), any(BigDecimal.class));
    }

    @Test
    void testWithdraw_NullWallet() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(null);

        // Act
        boolean result = walletService.withdraw(1, new BigDecimal("100.00"));

        // Assert
        assertFalse(result);
        verify(walletDAO, never()).updateBalance(anyInt(), any(BigDecimal.class));
    }
}
