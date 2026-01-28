package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.UserDAO;
import org.example.dao.WalletDAO;
import org.example.model.Transaction;
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
class TransactionServiceTest {

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private WalletDAO walletDAO;

    @Mock
    private UserDAO userDAO;

    private TransactionService transactionService;

    private Wallet testWallet;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionDAO, walletDAO, userDAO);
        testWallet = new Wallet();
        testWallet.setWalletId(1);
        testWallet.setUserId(1);

        testTransactions = new ArrayList<>();
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(1);
        transaction1.setFromWalletId(1);
        transaction1.setToWalletId(2);
        transaction1.setAmount(new BigDecimal("500.00"));
        transaction1.setTransactionType("SEND");
        transaction1.setStatus("SUCCESS");
        transaction1.setCreatedAt(java.time.LocalDateTime.now());
        testTransactions.add(transaction1);
    }

    @Test
    void testDisplayTransactionHistory_Success() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);
        when(transactionDAO.getTransactionsByWalletId(1)).thenReturn(testTransactions);

        // Act
        transactionService.displayTransactionHistory(1);

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, times(1)).getTransactionsByWalletId(1);
    }

    @Test
    void testDisplayTransactionHistory_NoWallet() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(null);

        // Act
        transactionService.displayTransactionHistory(1);

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, never()).getTransactionsByWalletId(anyInt());
    }

    @Test
    void testDisplayTransactionHistory_NoTransactions() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);
        when(transactionDAO.getTransactionsByWalletId(1)).thenReturn(new ArrayList<>());

        // Act
        transactionService.displayTransactionHistory(1);

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, times(1)).getTransactionsByWalletId(1);
    }

    @Test
    void testDisplayTransactionHistoryWithFilters() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(testWallet);
        when(transactionDAO.getTransactionsByWalletId(1)).thenReturn(testTransactions);

        // Act
        transactionService.displayTransactionHistory(1, "SEND", null, null, null, null, "SUCCESS", "");

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, times(1)).getTransactionsByWalletId(1);
    }
}
