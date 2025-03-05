package com.array.banking.controller;

import com.array.banking.dto.BalanceResponse;
import com.array.banking.dto.TransferRequest;
import com.array.banking.dto.TransferResponse;
import com.array.banking.model.Transaction;
import com.array.banking.model.User;
import com.array.banking.service.TransactionService;
import com.array.banking.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/banking/v1")
@Validated
@RequiredArgsConstructor
public class BankingController {
    
    private final UserService userService;
    
    private final TransactionService transactionService;
    
    /**
     * Get the authenticated user's current balance
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        User user = getCurrentUser();
        BalanceResponse response = new BalanceResponse(user.getUsername(), user.getBalance());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get paginated list of transactions for the authenticated user
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be negative") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must not be less than one") int size) {
        
        // Cap the maximum page size to prevent potential resource issues
        size = Math.min(size, 100);
        
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getUserTransactionsPaginated(user, pageable);
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Transfer funds to another user
     * Ensures overdrafts are not allowed
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest transferRequest) {
        try {
            User sender = getCurrentUser();
            String recipientUsername = transferRequest.getRecipientUsername();
            BigDecimal amount = transferRequest.getAmount();
            
            // Check for self-transfer
            if (sender.getUsername().equals(recipientUsername)) {
                return ResponseEntity.badRequest().body("Cannot transfer funds to yourself");
            }
            
            if (sender.getBalance().compareTo(amount) < 0) {
                return ResponseEntity.badRequest().body("Insufficient funds for transfer");
            }
            
            Optional<User> recipientOpt = userService.getUserByUsername(recipientUsername);
            if (!recipientOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Recipient not found");
            }
            
            User recipient = recipientOpt.get();
            Integer transactionId = transactionService.transfer(sender, recipient, amount);
            
            TransferResponse response = new TransferResponse(
                "Transfer successful",
                sender.getBalance(),
                transactionId
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
