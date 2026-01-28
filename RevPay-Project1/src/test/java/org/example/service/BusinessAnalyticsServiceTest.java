package org.example.service;

import org.example.dao.InvoiceDAO;
import org.example.dao.TransactionDAO;
import org.example.dao.WalletDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessAnalyticsServiceTest {

    @Mock
    private TransactionDAO transactionDAO;

    @Mock
    private InvoiceDAO invoiceDAO;

    @Mock
    private WalletDAO walletDAO;

    private BusinessAnalyticsService businessAnalyticsService;

    @BeforeEach
    void setUp() {
        businessAnalyticsService = new BusinessAnalyticsService(transactionDAO, invoiceDAO, walletDAO);
        // Setup mocks if needed
    }

    @Test
    void testDisplayBusinessAnalytics_NullWallet() {
        // Arrange
        when(walletDAO.getWalletByUserId(1)).thenReturn(null);

        // Act
        businessAnalyticsService.displayBusinessAnalytics(1);

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, never()).getTransactionsByWalletId(anyInt());
    }

    @Test
    void testDisplayBusinessAnalytics_Success() {
        // Arrange
        org.example.model.Wallet wallet = new org.example.model.Wallet();
        wallet.setWalletId(1);
        wallet.setUserId(1);
        
        when(walletDAO.getWalletByUserId(1)).thenReturn(wallet);
        when(transactionDAO.getTransactionsByWalletId(1)).thenReturn(new java.util.ArrayList<>());
        when(invoiceDAO.getInvoicesByBusinessUserId(1)).thenReturn(new java.util.ArrayList<>());

        // Act
        businessAnalyticsService.displayBusinessAnalytics(1);

        // Assert
        verify(walletDAO, times(1)).getWalletByUserId(1);
        verify(transactionDAO, times(3)).getTransactionsByWalletId(1); // Called 3 times for different metrics
        verify(invoiceDAO, times(2)).getInvoicesByBusinessUserId(1); // Called 2 times (invoice summary and top customers)
    }
}
