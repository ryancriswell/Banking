package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.util.CurrencyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BalanceService balanceService;

    public Page<Transaction> getUserTransactionsPaginated(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTimestampDescTransactionIdDesc(user, pageable);
    }
    
    /**
     * Complete a transaction by setting its status to COMPLETED
     * This method initiates an async operation and returns immediately
     */
    @Transactional
    public Transaction completeAndSaveTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Process asynchronously
        completeTransactionAsync(savedTransaction.getTransactionId());
        
        return savedTransaction;
    }

    /**
     * Async method to complete the transaction
     */
    @Async("transactionExecutor")
    @Transactional
    public CompletableFuture<Transaction> completeTransactionAsync(Integer transactionId) {
        try {
            log.info("Processing transaction completion asynchronously: {}", transactionId);
            Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
            
            // Simulate some processing time
            Thread.sleep(100);
            
            transaction.setStatus(TransactionStatus.COMPLETED);
            Transaction completed = transactionRepository.save(transaction);
            log.info("Successfully completed transaction: {}", transactionId);
            
            return CompletableFuture.completedFuture(completed);
        } catch (Exception e) {
            log.error("Error completing transaction {}: {}", transactionId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Fail a transaction by setting its status to FAILED
     * This method initiates an async operation and returns immediately
     */
    @Transactional
    public Transaction failAndSaveTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Process asynchronously
        failTransactionAsync(savedTransaction.getTransactionId());
        
        return savedTransaction;
    }

    /**
     * Async method to fail the transaction
     */
    @Async("transactionExecutor")
    @Transactional
    public CompletableFuture<Transaction> failTransactionAsync(Integer transactionId) {
        try {
            log.info("Processing transaction failure asynchronously: {}", transactionId);
            Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
            
            // Simulate some processing time
            Thread.sleep(100);
            
            transaction.setStatus(TransactionStatus.FAILED);
            Transaction failed = transactionRepository.save(transaction);
            log.info("Successfully marked transaction as failed: {}", transactionId);
            
            return CompletableFuture.completedFuture(failed);
        } catch (Exception e) {
            log.error("Error failing transaction {}: {}", transactionId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional()
    public Integer transfer(User sender, User recipient, BigDecimal amountInDollars) {
        Long amountInCents = CurrencyUtil.dollarsToCents(amountInDollars);

        if (amountInCents <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (sender.equals(recipient)) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        Transaction outgoing = new Transaction(sender, amountInCents, TransactionType.TRANSFER_OUT);
        Transaction incoming = new Transaction(recipient, amountInCents, TransactionType.TRANSFER_IN);

        if (!balanceService.hasSufficientBalance(sender, amountInCents)) {
            // Set both transactions as failed
            failAndSaveTransaction(outgoing);
            failAndSaveTransaction(incoming);
            throw new IllegalArgumentException("Transaction Failed: Insufficient funds for transfer");
        } else {
            completeAndSaveTransaction(outgoing);
            completeAndSaveTransaction(incoming);
        }

        return outgoing.getTransactionId();
    }
    
    @Transactional()
    public Long deposit(User user, BigDecimal amountInDollars) {
        Long amountInCents = CurrencyUtil.dollarsToCents(amountInDollars);
        Long currentBalance = balanceService.getCurrentBalanceInCents(user);
        
        Transaction transaction = new Transaction(user, amountInCents, TransactionType.DEPOSIT);
        completeAndSaveTransaction(transaction);
        
        return currentBalance + amountInCents;
    }

    @Transactional()
    public Long withdraw(User user, BigDecimal amountInDollars) {
        Long amountInCents = CurrencyUtil.dollarsToCents(amountInDollars);
        
        Long currentBalance = balanceService.getCurrentBalanceInCents(user);
        Transaction transaction = new Transaction(user, amountInCents, TransactionType.WITHDRAWAL);

        if (balanceService.hasSufficientBalance(user, amountInCents)) {
            completeAndSaveTransaction(transaction);
            return currentBalance - amountInCents;
        } else {
            log.warn("Withdrawal failed due to insufficient funds");
            failAndSaveTransaction(transaction);
            return currentBalance;
        }   
    }

}
