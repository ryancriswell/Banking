package com.array.banking.controller;

import com.array.banking.model.Transaction;
import com.array.banking.model.User;
import com.array.banking.service.TransactionService;
import com.array.banking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/banking/v1")
public class BankingController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * Get the authenticated user's current balance
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        User user = getCurrentUser();
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("balance", user.getBalance());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get paginated list of transactions for the authenticated user
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
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
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> transferRequest) {
        try {
            User sender = getCurrentUser();
            
            String recipientUsername = (String) transferRequest.get("recipientUsername");
            BigDecimal amount = new BigDecimal(transferRequest.get("amount").toString());
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Transfer amount must be positive");
            }
            
            if (sender.getBalance().compareTo(amount) < 0) {
                return ResponseEntity.badRequest().body("Insufficient funds for transfer");
            }
            
            Optional<User> recipientOpt = userService.getUserByUsername(recipientUsername);
            if (!recipientOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Recipient not found");
            }
            
            User recipient = recipientOpt.get();
            transactionService.transfer(sender, recipient, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transfer successful");
            response.put("newBalance", sender.getBalance());
            
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
