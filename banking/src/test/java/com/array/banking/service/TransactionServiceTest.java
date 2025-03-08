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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private BalanceService balanceService;
    
    @InjectMocks
    private TransactionService transactionService;
    
    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;
    
    private User sender;
    private User recipient;
    private Transaction transaction;
    
    @BeforeEach
    void setUp() {
        sender = new User("sender", "password", "sender@example.com");
        sender.setUserId(1);
        
        recipient = new User("recipient", "password", "recipient@example.com");
        recipient.setUserId(2);
        
        transaction = new Transaction(sender, 10000L, TransactionType.TRANSFER_OUT);
        transaction.setTransactionId(1);
    }
    
    @Test
    void getUserTransactionsPaginated_ShouldReturnTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = List.of(transaction);
        Page<Transaction> page = new PageImpl<>(transactions, pageable, 1);
        
        when(transactionRepository.findByUserOrderByTimestampDescTransactionIdDesc(sender, pageable))
            .thenReturn(page);
        
        Page<Transaction> result = transactionService.getUserTransactionsPaginated(sender, pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals(transaction, result.getContent().get(0));
        verify(transactionRepository).findByUserOrderByTimestampDescTransactionIdDesc(sender, pageable);
    }
    
    @Test
    void completeAndSaveTransaction_ShouldCompleteTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        Transaction result = transactionService.completeAndSaveTransaction(transaction);
        
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(transactionRepository).save(transaction);
    }
    
    @Test
    void failAndSaveTransaction_ShouldFailTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        Transaction result = transactionService.failAndSaveTransaction(transaction);
        
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(transactionRepository).save(transaction);
    }
    
    @Test
    void transfer_ShouldCompleteTransfer_WhenSufficientFunds() {
        when(balanceService.hasSufficientBalance(eq(sender), anyLong())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        transactionService.transfer(sender, recipient, new BigDecimal("100.00"));
        
        verify(balanceService).hasSufficientBalance(eq(sender), eq(10000L));
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        assertEquals(2, savedTransactions.size());
        
        Transaction outgoing = savedTransactions.get(0);
        Transaction incoming = savedTransactions.get(1);
        
        assertEquals(TransactionType.TRANSFER_OUT, outgoing.getType());
        assertEquals(TransactionType.TRANSFER_IN, incoming.getType());
        assertEquals(sender, outgoing.getUser());
        assertEquals(recipient, incoming.getUser());
        assertEquals(TransactionStatus.COMPLETED, outgoing.getStatus());
        assertEquals(TransactionStatus.COMPLETED, incoming.getStatus());
        assertEquals(10000L, outgoing.getAmount());
        assertEquals(10000L, incoming.getAmount());
    }
    
    @Test
    void transfer_ShouldFailTransfer_WhenInsufficientFunds() {
        when(balanceService.hasSufficientBalance(eq(sender), anyLong())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        assertThrows(IllegalArgumentException.class, () ->
            transactionService.transfer(sender, recipient, new BigDecimal("100.00"))
        );
        
        verify(balanceService).hasSufficientBalance(eq(sender), eq(10000L));
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        assertEquals(2, savedTransactions.size());
        
        Transaction outgoing = savedTransactions.get(0);
        Transaction incoming = savedTransactions.get(1);
        
        assertEquals(TransactionType.TRANSFER_OUT, outgoing.getType());
        assertEquals(TransactionType.TRANSFER_IN, incoming.getType());
        assertEquals(TransactionStatus.FAILED, outgoing.getStatus());
        assertEquals(TransactionStatus.FAILED, incoming.getStatus());
    }
    
    @Test
    void transfer_ShouldThrowException_WhenTransferToSelf() {
        assertThrows(IllegalArgumentException.class, () ->
            transactionService.transfer(sender, sender, new BigDecimal("100.00"))
        );
        
        verifyNoInteractions(balanceService);
        verifyNoInteractions(transactionRepository);
    }
    
    @Test
    void deposit_ShouldCompleteDeposit() {
        when(balanceService.getCurrentBalanceInCents(sender)).thenReturn(5000L);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Long newBalance = transactionService.deposit(sender, new BigDecimal("100.00"));
        
        assertEquals(15000L, newBalance);
        verify(balanceService).getCurrentBalanceInCents(sender);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionType.DEPOSIT, savedTransaction.getType());
        assertEquals(sender, savedTransaction.getUser());
        assertEquals(10000L, savedTransaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
    }
    
    @Test
    void withdraw_ShouldCompleteWithdrawal_WhenSufficientFunds() {
        when(balanceService.getCurrentBalanceInCents(sender)).thenReturn(20000L);
        when(balanceService.hasSufficientBalance(eq(sender), anyLong())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Long newBalance = transactionService.withdraw(sender, new BigDecimal("100.00"));
        
        assertEquals(10000L, newBalance);
        verify(balanceService).getCurrentBalanceInCents(sender);
        verify(balanceService).hasSufficientBalance(eq(sender), eq(10000L));
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, savedTransaction.getType());
        assertEquals(sender, savedTransaction.getUser());
        assertEquals(10000L, savedTransaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
    }
    
    @Test
    void withdraw_ShouldFailWithdrawal_WhenInsufficientFunds() {
        when(balanceService.getCurrentBalanceInCents(sender)).thenReturn(5000L);
        when(balanceService.hasSufficientBalance(eq(sender), anyLong())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        Long newBalance = transactionService.withdraw(sender, new BigDecimal("100.00"));
        
        assertEquals(5000L, newBalance);
        verify(balanceService).getCurrentBalanceInCents(sender);
        verify(balanceService).hasSufficientBalance(eq(sender), eq(10000L));
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, savedTransaction.getType());
        assertEquals(sender, savedTransaction.getUser());
        assertEquals(10000L, savedTransaction.getAmount());
        assertEquals(TransactionStatus.FAILED, savedTransaction.getStatus());
    }
}
