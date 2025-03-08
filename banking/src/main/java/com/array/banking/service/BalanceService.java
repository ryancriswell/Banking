package com.array.banking.service;

import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.util.CurrencyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
     
}
