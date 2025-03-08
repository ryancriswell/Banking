package com.array.banking.service;

import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import com.array.banking.util.CurrencyUtil;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceService {

    private final TransactionRepository transactionRepository;
    private final MeterRegistry meterRegistry;
    
    /**
     * Get the current balance for a user by calculating from transactions (in cents)
     */
    public Long getCurrentBalanceInCents(User user) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return transactionRepository.calculateBalanceForUser(user.getUserId());
        } finally {
            sample.stop(Timer.builder("banking.balance.get_cents")
                    .tag("user_id", user.getUserId().toString())
                    .description("Time taken to get user balance in cents")
                    .register(meterRegistry));
        }
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
