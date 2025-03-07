package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.util.CurrencyUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TransactionRepository transactionRepository;
    
    /**
     * Get the current balance for a user by calculating from transactions (in cents)
     */
    public Long getCurrentBalanceInCents(User user) {
      return transactionRepository.calculateBalanceForUser(user.getUserId());
    }

    // BigDecimal response for the frontend display
    public BigDecimal getCurrentBalanceInDollars(User user) {
        return CurrencyUtil.centsToDollars(getCurrentBalanceInCents(user));
    }
    
    /**
     * Check if a user has sufficient balance for a withdrawal or transfer
     */
    public boolean hasSufficientBalance(User user, Long amountInCents) {
        Long balance = getCurrentBalanceInCents(user);
        return balance >= amountInCents;
    }
    
    /**
     * Calculate what the user's balance would be after a transaction (in cents)
     */
    public Long calculateBalanceAfterTransaction(User user, Long amountInCents, TransactionType type) {
      Long currentBalance = getCurrentBalanceInCents(user);
        
        if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN) {
            return currentBalance + amountInCents;
        } else if (type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER_OUT) {
            return currentBalance - amountInCents;
        }
        
        return currentBalance;
    }
    
    /**
     * Get the latest transaction for a user
     */
    public Transaction getLatestTransaction(User user) {
        List<Transaction> transactions = transactionRepository.findLatestCompletedTransactionByUser(
            user.getUserId(), PageRequest.of(0, 1));
        return transactions.isEmpty() ? null : transactions.get(0);
    }
    
    /**
     * Get the balance before a specific transaction (in cents)
     */
    public Long calculateBalanceBeforeTransaction(User user, Integer transactionId) {
      return transactionRepository.calculateBalanceForUserBeforeTransaction(user.getUserId(), transactionId);
    }
        
}
