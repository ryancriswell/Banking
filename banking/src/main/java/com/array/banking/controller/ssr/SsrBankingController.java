package com.array.banking.controller.ssr;

import com.array.banking.dto.TransferRequest;
import com.array.banking.model.Transaction;
import com.array.banking.model.User;
import com.array.banking.service.BalanceService;
import com.array.banking.service.TransactionService;
import com.array.banking.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ssr")
@Slf4j
@Validated
@RequiredArgsConstructor
public class SsrBankingController {
    
    private final UserService userService;
    private final BalanceService balanceService;
    private final TransactionService transactionService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> transactions = transactionService.getUserTransactionsPaginated(user, pageable);
        model.addAttribute("user", user);
        model.addAttribute("balance", balanceService.getCurrentBalanceInDollars(user));
        model.addAttribute("recentTransactions", transactions.getContent());
        return "ssr/dashboard";
    }
    
    @GetMapping("/transactions")
    public String getTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            Model model) {
        
        // Cap the maximum page size
        size = Math.min(size, 100);
        
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getUserTransactionsPaginated(user, pageable);
        
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        
        // For partial HTMX updates
        if (isHtmxRequest()) {
            return "ssr/fragments/transaction-list";
        }
        
        return "ssr/transactions";
    }
    
    @GetMapping("/transfer")
    public String transferForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("balance", balanceService.getCurrentBalanceInDollars(user));
        model.addAttribute("transferRequest", new TransferRequest());
        return "ssr/transfer";
    }
    
    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest transferRequest,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("balance", balanceService.getCurrentBalanceInDollars(user));
        
        if (bindingResult.hasErrors()) {
            log.error("Transfer request has errors: {}", bindingResult.getAllErrors());
            return "ssr/transfer";
        }
        
        try {
            String recipientUsername = transferRequest.getRecipientUsername();
            BigDecimal amount = transferRequest.getAmount();
            
            // Check for self-transfer
            if (user.getUsername().equals(recipientUsername)) {
                model.addAttribute("error", "Cannot transfer funds to yourself");
                return "ssr/transfer";
            }
            
            // Check for sufficient funds
            if (balanceService.getCurrentBalanceInDollars(user).compareTo(amount) < 0) {
                model.addAttribute("error", "Insufficient funds for transfer");
                return "ssr/transfer";
            }
            
            // Check if recipient exists
            Optional<User> recipientOpt = userService.getUserByUsername(recipientUsername);
            if (recipientOpt.isEmpty()) {
                model.addAttribute("error", "Recipient not found");
                return "ssr/transfer";
            }
            
            // Perform transfer
            User recipient = recipientOpt.get();
            transactionService.transfer(user, recipient, amount);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Transfer successful. Your new balance is $" + balanceService.getCurrentBalanceInDollars(user));
            
            // For HTMX requests, return a fragment with the new balance
            if (isHtmxRequest()) {
                model.addAttribute("balance", balanceService.getCurrentBalanceInDollars(user));
                return "ssr/fragments/balance";
            }
            
            return "redirect:/ssr/dashboard";
        } catch (Exception e) {
            log.error("Transfer failed with exception", e);
            model.addAttribute("error", "Transfer failed: " + e.getMessage());
            return "ssr/transfer";
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    

    private boolean isHtmxRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return request.getHeader("HX-Request") != null;
        }
        return false;
    }
}