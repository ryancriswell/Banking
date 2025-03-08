package com.array.banking.service;

import com.array.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RandomTransactionServiceTest {

    @Mock
    private TransactionService transactionService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private RandomTransactionService randomTransactionService;
    
    private User testUser;
    private User randomUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedpassword", "test@example.com");
        testUser.setUserId(1);
        
        randomUser = new User("randomuser", "hashedpassword", "random@example.com");
        randomUser.setUserId(2);
    }
    
    @Test
    void generateRandomTransactions_ShouldGenerateTransactions() {
        // We can't test the random behavior precisely, but we can verify interactions
        when(userService.findRandomUser()).thenReturn(randomUser);
        when(transactionService.deposit(eq(testUser), any(BigDecimal.class))).thenReturn(10000L);
        when(transactionService.withdraw(eq(testUser), any(BigDecimal.class))).thenReturn(5000L);
        when(transactionService.transfer(any(User.class), any(User.class), any(BigDecimal.class))).thenReturn(1);
        
        randomTransactionService.generateRandomTransactions(testUser);
        
        // Verify at least some interactions occurred
        verify(userService, atLeastOnce()).findRandomUser();
        verify(transactionService, atLeastOnce()).deposit(eq(testUser), any(BigDecimal.class));
        verify(transactionService, atLeastOnce()).withdraw(eq(testUser), any(BigDecimal.class));
        verify(transactionService, atLeastOnce()).transfer(any(User.class), any(User.class), any(BigDecimal.class));
    }
}
