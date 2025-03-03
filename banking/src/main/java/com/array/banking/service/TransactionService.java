package com.array.banking.service;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import com.array.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

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
    public void transfer(User sender, User recipient, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        // Update balances
        userService.updateUserBalance(sender, sender.getBalance().subtract(amount));
        userService.updateUserBalance(recipient, recipient.getBalance().add(amount));
        

        // Create transaction records
        Transaction outgoing = new Transaction(sender, amount, TransactionType.TRANSFER_OUT);
        Transaction incoming = new Transaction(recipient, amount, TransactionType.TRANSFER_IN);
        
        transactionRepository.save(outgoing);
        transactionRepository.save(incoming);
    }
}
