package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RandomTransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final Random random = new Random();

    @Transactional
    public void generateRandomTransactions(User user) {
        int transactionCount = random.nextInt(10) + 1; // Generate 1-10 transactions
        
        for (int i = 0; i < transactionCount; i++) {
            TransactionType type = getRandomTransactionType();
            BigDecimal amount = getRandomAmount(type);
            LocalDateTime timestamp = LocalDateTime.now();
            
            // Create and save transaction
            Transaction transaction = new Transaction(user, amount, type);
            transaction.setTimestamp(timestamp);
            transactionRepository.save(transaction);
            
            // Update user balance based on transaction type
            executeTransaction(user, amount, type);
        }
        
        // Save updated user balance
        userService.saveUser(user);
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
       
    private void executeTransaction(User user, BigDecimal amount, TransactionType type) {
      BigDecimal currentBalance = user.getBalance();
        
      try {
        switch (type) {
          case DEPOSIT:
            userService.updateUserBalance(user, currentBalance.add(amount));
            break;
          case WITHDRAWAL:
            // TODO: handle errors if user has insufficient funds
            // userService.updateUserBalance(user, currentBalance.subtract(amount));
            break;
          case TRANSFER_IN:
            // TODO: can potentially throw an exception if no users are found
            // TODO: can potentially transfer to the same user
            // transactionService.transfer(userService.findRandomUser(), user, amount);
            break;
          case TRANSFER_OUT:
            // TODO: can potentially throw an exception if no users are found
            // TODO: can potentially transfer to the same user
            // transactionService.transfer(user, userService.findRandomUser(), amount);
            break;
        }
      // TODO: real application should handle exceptions more gracefully
      } catch (IllegalArgumentException | UnexpectedRollbackException e) {
        // Log the exception and continue, likely caused by insufficient funds for transfer in new DB
        log.error(e.getMessage());
      }
    }
}
