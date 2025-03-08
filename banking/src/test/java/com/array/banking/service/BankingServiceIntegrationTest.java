package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.repository.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// This test class writes and flushes the real data to the database. It should only be ran in a test environment.
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BankingServiceIntegrationTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private BalanceService balanceService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User sender;
    private User recipient;
    
    @BeforeEach
    void setUp() {
      sender = userService.createUser("sender", "password", "sender@example.com");
      recipient = userService.createUser("recipient", "password", "recipient@example.com");
    }

    @AfterEach
    void tearDown() {
      userRepository.deleteAll();
      transactionRepository.deleteAll();
    }
    
    @Test
    @Transactional
    void testCompleteUserTransactionFlow() {
        // Initial deposit to sender
        Long senderBalance = transactionService.deposit(sender, new BigDecimal("500.00"));
        
        // Check balance was updated correctly
        assertEquals(50000L, senderBalance);
        assertEquals(new BigDecimal("500.00"), balanceService.getCurrentBalanceInDollars(sender));
        
        // Transfer from sender to recipient
        transactionService.transfer(sender, recipient, new BigDecimal("200.00"));
        
        // Verify balances after transfer
        assertEquals(new BigDecimal("300.00"), balanceService.getCurrentBalanceInDollars(sender));
        assertEquals(new BigDecimal("200.00"), balanceService.getCurrentBalanceInDollars(recipient));
        
        // Withdrawal attempt exceeding balance
        Long balanceAfterFailedWithdrawal = transactionService.withdraw(sender, new BigDecimal("400.00"));
        assertEquals(30000L, balanceAfterFailedWithdrawal);
        assertEquals(new BigDecimal("300.00"), balanceService.getCurrentBalanceInDollars(sender));
        
        // Successful withdrawal
        Long balanceAfterWithdrawal = transactionService.withdraw(sender, new BigDecimal("100.00"));
        assertEquals(20000L, balanceAfterWithdrawal);
        assertEquals(new BigDecimal("200.00"), balanceService.getCurrentBalanceInDollars(sender));
        
        // Verify transaction history
        Page<Transaction> senderTransactions = transactionService.getUserTransactionsPaginated(
            sender, PageRequest.of(0, 10));
        Page<Transaction> recipientTransactions = transactionService.getUserTransactionsPaginated(
          recipient, PageRequest.of(0, 10));

        // All sender transactions
        assertEquals(4, senderTransactions.getTotalElements());
        // Recipient should have a single transaction for the transfer-in
        assertEquals(1, recipientTransactions.getTotalElements());

        
        // Verify transaction details
        boolean foundDeposit = false;
        boolean foundTransfer = false;
        boolean foundWithdrawal = false;
        
        for (Transaction t : senderTransactions) {
            if (t.getType() == TransactionType.DEPOSIT && t.getAmount() == 50000L) {
                foundDeposit = true;
                assertEquals(TransactionStatus.COMPLETED, t.getStatus());
            }
            if (t.getType() == TransactionType.TRANSFER_OUT && t.getAmount() == 20000L) {
                foundTransfer = true;
                assertEquals(TransactionStatus.COMPLETED, t.getStatus());
            }
            if (t.getType() == TransactionType.WITHDRAWAL && t.getAmount() == 10000L) {
                foundWithdrawal = true;
                assertEquals(TransactionStatus.COMPLETED, t.getStatus());
            }
        }
        
        assertTrue(foundDeposit);
        assertTrue(foundTransfer);
        assertTrue(foundWithdrawal);
    }
    
    @Test
    @Transactional
    void testInsufficientFundsTransactionFlow() {
        // Deposit small amount
        transactionService.deposit(sender, new BigDecimal("50.00"));
        
        // Attempt transfer exceeding balance
        assertThrows(IllegalArgumentException.class, () ->
            transactionService.transfer(sender, recipient, new BigDecimal("100.00"))
        );
        
        // Verify failed transfer in transactions
        Page<Transaction> senderTransactions = transactionService.getUserTransactionsPaginated(
            sender, PageRequest.of(0, 10));
        
        boolean foundFailedTransfer = false;
        for (Transaction t : senderTransactions) {
            if (t.getType() == TransactionType.TRANSFER_OUT && t.getAmount() == 10000L) {
                foundFailedTransfer = true;
                assertEquals(TransactionStatus.FAILED, t.getStatus());
            }
        }
        
        assertTrue(foundFailedTransfer);
        
        // Balances should be unchanged after failed transfer
        assertEquals(new BigDecimal("50.00"), balanceService.getCurrentBalanceInDollars(sender));
        assertEquals(new BigDecimal("0.00"), balanceService.getCurrentBalanceInDollars(recipient));
    }
    
    @Test
    @Transactional
    void testUserCreateAndAuthenticationFlow() {
        // Test user creation
        User newUser = userService.createUser("testuser", "password123", "test@example.com");
        assertNotNull(newUser);
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        
        // Test fetching user by username
        Optional<User> fetchedByUsername = userService.getUserByUsername("testuser");
        assertTrue(fetchedByUsername.isPresent());
        assertEquals(newUser.getUserId(), fetchedByUsername.get().getUserId());
        
        // Test fetching user by email
        Optional<User> fetchedByEmail = userService.getUserByEmail("test@example.com");
        assertTrue(fetchedByEmail.isPresent());
        assertEquals(newUser.getUserId(), fetchedByEmail.get().getUserId());
        
        // Test authentication
        UserDetails userDetails = userService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertTrue(passwordEncoder.matches("password123", userDetails.getPassword()));
        
        // Test duplicate username rejection
        Exception usernameException = assertThrows(IllegalArgumentException.class, () ->
            userService.createUser("testuser", "password456", "another@example.com")
        );
        assertEquals("Username already exists", usernameException.getMessage());
        
        // Test duplicate email rejection
        Exception emailException = assertThrows(IllegalArgumentException.class, () ->
            userService.createUser("anotheruser", "password456", "test@example.com")
        );
        assertEquals("Email already exists", emailException.getMessage());
    }
    
    @Test
    @Transactional
    void testRandomUserSelection() {
        // Create a few users
        User user1 = userService.createUser("user1", "password", "user1@example.com");
        User user2 = userService.createUser("user2", "password", "user2@example.com");
        User user3 = userService.createUser("user3", "password", "user3@example.com");
        
        // Find random user multiple times
        boolean foundUser1 = false;
        boolean foundUser2 = false;
        boolean foundUser3 = false;
        
        // Since random selection is being tested, we need multiple trials
        for (int i = 0; i < 100; i++) {
            User randomUser = userService.findRandomUser();
            assertNotNull(randomUser);
            
            if (randomUser.getUserId().equals(user1.getUserId())) foundUser1 = true;
            if (randomUser.getUserId().equals(user2.getUserId())) foundUser2 = true;
            if (randomUser.getUserId().equals(user3.getUserId())) foundUser3 = true;
            
            // If we've found all users, we can break early
            if (foundUser1 && foundUser2 && foundUser3) break;
        }
        
        // With enough trials, we should have found each user at least once
        // (This is probabilistic but very likely with 100 trials and 3 users)
        assertTrue(foundUser1 && foundUser2 && foundUser3, "At least one random user should be found");
    }
}
