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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
     */
    @Transactional()
    public Transaction completeAndSaveTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    /**
     * Fail a transaction by setting its status to FAILED
     */
    @Transactional()
    public Transaction failAndSaveTransaction(Transaction transaction) {
        // For failed transactions, the balance doesn't change
        transaction.setStatus(TransactionStatus.FAILED);
        return transactionRepository.save(transaction);
    }

    @Transactional()
    public Integer transfer(User sender, User recipient, BigDecimal amountInDollars) {
        Long amountInCents = CurrencyUtil.dollarsToCents(amountInDollars);
        log.info("Transferring {} cents from {} to {}", amountInCents, sender.getUsername(), recipient.getUsername());

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
        log.info("Depositing {} cents for {}", amountInCents, user.getUsername());
        
        Transaction transaction = new Transaction(user, amountInCents, TransactionType.DEPOSIT);
        completeAndSaveTransaction(transaction);
        
        return currentBalance + amountInCents;
    }

    @Transactional()
    public Long withdraw(User user, BigDecimal amountInDollars) {
        Long amountInCents = CurrencyUtil.dollarsToCents(amountInDollars);
        log.info("Withdrawing {} cents from {}", amountInCents, user.getUsername());
        
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
