package com.array.banking.service;

import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private BalanceService balanceService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedpassword", "test@example.com");
        testUser.setUserId(1);
    }
    
    @Test
    void getCurrentBalanceInCents_ShouldReturnCorrectBalance() {
        when(transactionRepository.calculateBalanceForUser(testUser.getUserId())).thenReturn(10000L);
        
        Long balance = balanceService.getCurrentBalanceInCents(testUser);
        
        assertEquals(10000L, balance);
        verify(transactionRepository).calculateBalanceForUser(testUser.getUserId());
    }
    
    @Test
    void getCurrentBalanceInDollars_ShouldReturnCorrectBalance() {
        when(transactionRepository.calculateBalanceForUser(testUser.getUserId())).thenReturn(10000L);
        
        BigDecimal balance = balanceService.getCurrentBalanceInDollars(testUser);
        
        assertEquals(new BigDecimal("100.00"), balance);
        verify(transactionRepository).calculateBalanceForUser(testUser.getUserId());
    }
    
    @Test
    void hasSufficientBalance_ShouldReturnTrue_WhenBalanceIsGreaterThanAmount() {
        when(transactionRepository.calculateBalanceForUser(testUser.getUserId())).thenReturn(10000L);
        
        boolean result = balanceService.hasSufficientBalance(testUser, 5000L);
        
        assertTrue(result);
        verify(transactionRepository).calculateBalanceForUser(testUser.getUserId());
    }
    
    @Test
    void hasSufficientBalance_ShouldReturnTrue_WhenBalanceEqualsAmount() {
        when(transactionRepository.calculateBalanceForUser(testUser.getUserId())).thenReturn(10000L);
        
        boolean result = balanceService.hasSufficientBalance(testUser, 10000L);
        
        assertTrue(result);
        verify(transactionRepository).calculateBalanceForUser(testUser.getUserId());
    }
    
    @Test
    void hasSufficientBalance_ShouldReturnFalse_WhenBalanceIsLessThanAmount() {
        when(transactionRepository.calculateBalanceForUser(testUser.getUserId())).thenReturn(5000L);
        
        boolean result = balanceService.hasSufficientBalance(testUser, 10000L);
        
        assertFalse(result);
        verify(transactionRepository).calculateBalanceForUser(testUser.getUserId());
    }
}
