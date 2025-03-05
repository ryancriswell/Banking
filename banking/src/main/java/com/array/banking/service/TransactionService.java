package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<Transaction> getUserTransactions(User user) {
        return transactionRepository.findByUser(user);
    }

    public Page<Transaction> getUserTransactionsPaginated(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByTimestampDesc(user, pageable);
    }

    public List<Transaction> getRecentTransactions(User user) {
        return transactionRepository.findByUserOrderByTimestampDesc(user);
    }
    
    @Transactional
    public Transaction completeAndSaveTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction failAndSaveTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Integer transfer(User sender, User recipient, BigDecimal amount) {
        log.info("Transferring ${} from {} to {}", amount, sender.getUsername(), recipient.getUsername());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (sender.equals(recipient)) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        // Calculate new balances
        BigDecimal senderNewBalance = sender.getBalance().subtract(amount);
        BigDecimal recipientNewBalance = recipient.getBalance().add(amount);

        // Create transaction records with the updated balances
        Transaction outgoing = new Transaction(sender, amount, TransactionType.TRANSFER_OUT, senderNewBalance);
        Transaction incoming = new Transaction(recipient, amount, TransactionType.TRANSFER_IN, recipientNewBalance);

        if (sender.getBalance().compareTo(amount) < 0) {
            // Still persist failed transactions without updating user balances
            failAndSaveTransaction(outgoing);
            failAndSaveTransaction(incoming);
            throw new IllegalArgumentException("Transaction Failed: Insufficient funds for transfer");
        } else {
            // Mark transactions as completed fragilly. What if the transaction fails at the DB level?
            completeAndSaveTransaction(outgoing);
            completeAndSaveTransaction(incoming);
        }

        // Update user balances
        userService.updateUserBalance(sender, senderNewBalance);
        userService.updateUserBalance(recipient, recipientNewBalance);
        return outgoing.getTransactionId();
    }
    
    @Transactional
    public BigDecimal deposit(User user, BigDecimal amount) {
        log.info("Depositing ${} for {}", amount, user.getUsername());
        BigDecimal currentBalance = user.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        Transaction transaction = new Transaction(user, amount, TransactionType.DEPOSIT, newBalance);
        
        userService.updateUserBalance(user, newBalance);
        completeAndSaveTransaction(transaction);
        return newBalance;
    }

    // Non-transfer transactions
    @Transactional
    public BigDecimal withdraw(User user, BigDecimal amount) {
        log.info("Withdrawing ${} from {}", amount, user.getUsername());
        BigDecimal currentBalance = user.getBalance();

        if (currentBalance.compareTo(amount) >= 0) {
            BigDecimal newBalance = currentBalance.subtract(amount);
            Transaction transaction = new Transaction(user, amount, TransactionType.WITHDRAWAL, newBalance);
            userService.updateUserBalance(user, newBalance);
            completeAndSaveTransaction(transaction);
            return newBalance;
        } else {
            log.warn("Withdrawal failed due to insufficient funds");
            Transaction transaction = new Transaction(user, amount, TransactionType.WITHDRAWAL, currentBalance);
            failAndSaveTransaction(transaction);
            // Return current balance if withdrawal fails
            return currentBalance;
        }   
    }
    
}
