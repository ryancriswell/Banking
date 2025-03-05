package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private User sender;
    private User recipient;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        sender = new User("sender", "password", "sender@example.com");
        sender.setUserId(1);
        sender.setBalance(new BigDecimal("1000.00"));

        recipient = new User("recipient", "password", "recipient@example.com");
        recipient.setUserId(2);
        recipient.setBalance(new BigDecimal("500.00"));

        amount = new BigDecimal("100.00");
    }

    @Test
    void transfer_ShouldSucceed_WhenSenderHasSufficientFunds() {
        // Setup
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        transactionService.transfer(sender, recipient, amount);
       
        // Verify transactions were saved
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        List<Transaction> capturedTransactions = transactionCaptor.getAllValues();
        
        assertEquals(2, capturedTransactions.size());
        
        // Check outgoing transaction
        Transaction outgoingTx = capturedTransactions.get(0);
        assertEquals(sender, outgoingTx.getUser());
        assertEquals(TransactionType.TRANSFER_OUT, outgoingTx.getType());
        assertEquals(amount, outgoingTx.getAmount());
        assertEquals(TransactionStatus.COMPLETED, outgoingTx.getStatus());
        assertEquals(new BigDecimal("900.00"), outgoingTx.getBalanceAfter());
        
        // Check incoming transaction
        Transaction incomingTx = capturedTransactions.get(1);
        assertEquals(recipient, incomingTx.getUser());
        assertEquals(TransactionType.TRANSFER_IN, incomingTx.getType());
        assertEquals(amount, incomingTx.getAmount());
        assertEquals(TransactionStatus.COMPLETED, incomingTx.getStatus());
        assertEquals(new BigDecimal("600.00"), incomingTx.getBalanceAfter());
        
        // Verify balances were updated
        verify(userService).updateUserBalance(sender, new BigDecimal("900.00"));
        verify(userService).updateUserBalance(recipient, new BigDecimal("600.00"));
    }

    @Test
    void transfer_ShouldThrowException_WhenSenderHasInsufficientFunds() {
        // Setup
        BigDecimal largeAmount = new BigDecimal("2000.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Execute & Verify
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transfer(sender, recipient, largeAmount);
        });
        
        assertEquals("Transaction Failed: Insufficient funds for transfer", exception.getMessage());
        
        // Verify failed transactions are recorded with FAILED status
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        List<Transaction> capturedTransactions = transactionCaptor.getAllValues();
        
        assertEquals(2, capturedTransactions.size());
        capturedTransactions.forEach(tx -> assertEquals(TransactionStatus.FAILED, tx.getStatus()));
        
        // Verify balances were NOT updated
        verify(userService, never()).updateUserBalance(any(User.class), any(BigDecimal.class));
    }

    @Test
    void transfer_ShouldThrowException_WhenTransferringToSelf() {
        // Execute & Verify
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transfer(sender, sender, amount);
        });
        
        assertEquals("Cannot transfer to self", exception.getMessage());
        
        // Verify no transactions were created
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(userService, never()).updateUserBalance(any(User.class), any(BigDecimal.class));
    }

    @Test
    void deposit_ShouldUpdateBalance_WhenAmountIsPositive() {
        // Setup
        BigDecimal depositAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("1200.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        BigDecimal newBalance = transactionService.deposit(sender, depositAmount);

        // Verify
        assertEquals(expectedBalance, newBalance);
        
        // Verify transaction was created
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction capturedTransaction = transactionCaptor.getValue();
        
        assertEquals(sender, capturedTransaction.getUser());
        assertEquals(TransactionType.DEPOSIT, capturedTransaction.getType());
        assertEquals(depositAmount, capturedTransaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, capturedTransaction.getStatus());
        assertEquals(expectedBalance, capturedTransaction.getBalanceAfter());
        
        // Verify balance was updated
        verify(userService).updateUserBalance(sender, expectedBalance);
    }

    @Test
    void withdraw_ShouldReturnUpdatedBalance_WhenSufficientFunds() {
        // Setup
        BigDecimal withdrawAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("800.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        BigDecimal newBalance = transactionService.withdraw(sender, withdrawAmount);

        // Verify
        assertEquals(expectedBalance, newBalance);
        
        // Verify transaction was created and completed
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction capturedTransaction = transactionCaptor.getValue();
        
        assertEquals(TransactionStatus.COMPLETED, capturedTransaction.getStatus());
        assertEquals(expectedBalance, capturedTransaction.getBalanceAfter());
        
        // Verify balance was updated
        verify(userService).updateUserBalance(sender, expectedBalance);
    }

    @Test
    void withdraw_ShouldReturnCurrentBalance_WhenInsufficientFunds() {
        // Setup
        BigDecimal largeAmount = new BigDecimal("2000.00");
        BigDecimal currentBalance = sender.getBalance();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        BigDecimal returnedBalance = transactionService.withdraw(sender, largeAmount);

        // Verify
        assertEquals(currentBalance, returnedBalance);
        
        // Verify transaction was created but failed
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction capturedTransaction = transactionCaptor.getValue();
        
        assertEquals(TransactionStatus.FAILED, capturedTransaction.getStatus());
        
        // Verify balance was NOT updated
        verify(userService, never()).updateUserBalance(any(User.class), any(BigDecimal.class));
    }

    @Test
    void getUserTransactionsPaginated_ShouldReturnPagedTransactions() {
        // Setup
        List<Transaction> transactionList = new ArrayList<>();
        Transaction transaction = new Transaction(sender, amount, TransactionType.DEPOSIT, sender.getBalance());
        transactionList.add(transaction);
        
        Page<Transaction> transactionPage = new PageImpl<>(transactionList);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(transactionRepository.findByUserOrderByTimestampDesc(sender, pageable)).thenReturn(transactionPage);

        // Execute
        Page<Transaction> result = transactionService.getUserTransactionsPaginated(sender, pageable);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(transactionRepository).findByUserOrderByTimestampDesc(sender, pageable);
    }
}
