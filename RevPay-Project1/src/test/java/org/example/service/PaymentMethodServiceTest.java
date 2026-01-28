package org.example.service;

import org.example.dao.PaymentMethodDAO;
import org.example.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodDAO paymentMethodDAO;

    private PaymentMethodService paymentMethodService;

    private PaymentMethod testPaymentMethod;

    @BeforeEach
    void setUp() {
        paymentMethodService = new PaymentMethodService(paymentMethodDAO);
        testPaymentMethod = new PaymentMethod();
        testPaymentMethod.setPaymentId(1);
        testPaymentMethod.setUserId(1);
        testPaymentMethod.setMethodType("CARD");
        testPaymentMethod.setDefault(false);
    }

    @Test
    void testValidateCardNumber_Valid() {
        // Act & Assert
        assertTrue(paymentMethodService.validateCardNumber("1234567890123")); // 13 digits
        assertTrue(paymentMethodService.validateCardNumber("1234567890123456")); // 16 digits
        assertTrue(paymentMethodService.validateCardNumber("1234567890123456789")); // 19 digits
    }

    @Test
    void testValidateCardNumber_Invalid() {
        // Act & Assert
        assertFalse(paymentMethodService.validateCardNumber("123456789012")); // Too short
        assertFalse(paymentMethodService.validateCardNumber("12345678901234567890")); // Too long
        assertFalse(paymentMethodService.validateCardNumber("123456789abcd")); // Non-numeric
        assertFalse(paymentMethodService.validateCardNumber("")); // Empty
        assertFalse(paymentMethodService.validateCardNumber(null)); // Null
    }

    @Test
    void testAddCard_Success() {
        // Arrange
        when(paymentMethodDAO.addPaymentMethod(any(PaymentMethod.class))).thenReturn(true);

        // Act
        boolean result = paymentMethodService.addCard(1, "CREDIT", "1234567890123456", 
            "John Doe", "12/25", "123", false);

        // Assert
        assertTrue(result);
        verify(paymentMethodDAO, times(1)).addPaymentMethod(any(PaymentMethod.class));
    }

    @Test
    void testAddCard_AsDefault() {
        // Arrange
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(testPaymentMethod);
        when(paymentMethodDAO.addPaymentMethod(any(PaymentMethod.class))).thenReturn(true);
        when(paymentMethodDAO.getPaymentMethodsByUserId(1)).thenReturn(methods);
        when(paymentMethodDAO.setDefaultPaymentMethod(1, 1)).thenReturn(true);

        // Act
        boolean result = paymentMethodService.addCard(1, "CREDIT", "1234567890123456", 
            "John Doe", "12/25", "123", true);

        // Assert
        assertTrue(result);
        verify(paymentMethodDAO, times(1)).addPaymentMethod(any(PaymentMethod.class));
        verify(paymentMethodDAO, times(1)).setDefaultPaymentMethod(1, 1);
    }

    @Test
    void testGetUserPaymentMethods() {
        // Arrange
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(testPaymentMethod);
        when(paymentMethodDAO.getPaymentMethodsByUserId(1)).thenReturn(methods);

        // Act
        List<PaymentMethod> result = paymentMethodService.getUserPaymentMethods(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentMethodDAO, times(1)).getPaymentMethodsByUserId(1);
    }

    @Test
    void testSetDefaultCard() {
        // Arrange
        when(paymentMethodDAO.setDefaultPaymentMethod(1, 1)).thenReturn(true);

        // Act
        boolean result = paymentMethodService.setDefaultCard(1, 1);

        // Assert
        assertTrue(result);
        verify(paymentMethodDAO, times(1)).setDefaultPaymentMethod(1, 1);
    }

    @Test
    void testDeleteCard() {
        // Arrange
        when(paymentMethodDAO.deletePaymentMethod(1, 1)).thenReturn(true);

        // Act
        boolean result = paymentMethodService.deleteCard(1, 1);

        // Assert
        assertTrue(result);
        verify(paymentMethodDAO, times(1)).deletePaymentMethod(1, 1);
    }

    @Test
    void testGetDefaultPaymentMethod() {
        // Arrange
        testPaymentMethod.setDefault(true);
        when(paymentMethodDAO.getDefaultPaymentMethod(1)).thenReturn(testPaymentMethod);

        // Act
        PaymentMethod result = paymentMethodService.getDefaultPaymentMethod(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isDefault());
        verify(paymentMethodDAO, times(1)).getDefaultPaymentMethod(1);
    }
}
