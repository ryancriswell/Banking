package com.array.banking.service;

import com.array.banking.model.TransactionType;
import com.array.banking.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RandomTransactionService {

    private final TransactionService transactionService;
    private final UserService userService;
    private final Random random = new Random();

    public void generateRandomTransactions(User user) {
        int transactionCount = random.nextInt(10) + 1; // Generate 1-10 transactions
        
        for (int i = 0; i < transactionCount; i++) {
          try {
            TransactionType type = getRandomTransactionType();
            BigDecimal amount = getRandomAmount(type);
            switch (type) {
                case DEPOSIT:
                    transactionService.deposit(user, amount);                     
                    break;
                  case WITHDRAWAL:
                    transactionService.withdraw(user, amount);
                    break;
                  case TRANSFER_IN:
                    // TODO: possible to transfer to self, should result in a failed transaction if so
                    User sender = userService.findRandomUser();
                    transactionService.transfer(sender, user, amount);
                    break;
                case TRANSFER_OUT:
                    // TODO: possible to transfer to self, should result in a failed transaction if so
                    User recipient = userService.findRandomUser();
                    transactionService.transfer(user, recipient, amount);
                    break;
            }
          } catch (Exception e) {
            // Since this is just used for testing, log and continue
            log.error("Failed to generate random transaction: {}", e.getMessage());
          }
        }
    }
    
    private TransactionType getRandomTransactionType() {
        TransactionType[] types = TransactionType.values();
        return types[random.nextInt(types.length)];
    }
    
    private BigDecimal getRandomAmount(TransactionType type) {
        // Generate random amounts between $0.01 and $1000.00
        double amount = 0.01 + (1000.00 - 0.01) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
       

}
