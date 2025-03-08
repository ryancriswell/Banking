package com.array.banking.service;

import com.array.banking.dto.BalancedTransaction;
import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.util.CurrencyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
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
     * Get the balance before a specific transaction in cents
     */
    public Long getBalanceAtTransactionInCents(User user, Integer transactionId) {
        Long balance = transactionRepository.calculateBalanceAtTransaction(user.getUserId(), transactionId);
        log.info("Balance at transaction {}: {} cents", transactionId, balance);
        return balance;
    }

    /**
     * Get the balance before a specific transaction in dollars
     * BigDecimal response for the frontend display
     */
    public BigDecimal getBalanceAtTransactionInDollars(User user, Integer transactionId) {
        return CurrencyUtil.centsToDollars(getBalanceAtTransactionInCents(user, transactionId));
    }

    /**
     * Create balanced transactions by calculating balance only once for the latest
     * transaction and then working backwards through the list.
     */
    public List<BalancedTransaction> balanceTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get balance for the first (most recent) transaction from database
        Transaction firstTransaction = transactions.get(0);
        User user = firstTransaction.getUser();
        BigDecimal balance = getBalanceAtTransactionInDollars(user, firstTransaction.getTransactionId());
        
        List<BalancedTransaction> result = new ArrayList<>();
        result.add(BalancedTransaction.fromTransaction(firstTransaction, balance));
        
        
        // For remaining transactions, calculate balance by working backwards
        for (int i = 1; i < transactions.size(); i++) { 
            Transaction currentTx = transactions.get(i);
            // Only calculate balance based on completed transactions
            if (currentTx.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }
            
            // Get the current transaction amount in dollars
            BigDecimal txAmount = CurrencyUtil.centsToDollars(currentTx.getAmount());
            
            // Update balance based on the current transaction's type
            // Since we're moving backwards in time, we need to reverse the effect of the current transaction
            if (currentTx.getType() == TransactionType.DEPOSIT || 
                currentTx.getType() == TransactionType.TRANSFER_IN) {
                balance = balance.subtract(txAmount);
            } else if (currentTx.getType() == TransactionType.WITHDRAWAL || 
                       currentTx.getType() == TransactionType.TRANSFER_OUT) {
                balance = balance.add(txAmount);
            }
            
            result.add(BalancedTransaction.fromTransaction(currentTx, balance));
        }
        
        return result;
    }
    
}
