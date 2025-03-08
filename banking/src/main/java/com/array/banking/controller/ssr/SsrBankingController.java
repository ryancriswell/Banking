package com.array.banking.controller.ssr;

import com.array.banking.dto.BalancedTransaction;
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
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// TODO: This is a lot of logic for a "controller", probably want to move most of it to an SsrService instead  
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
        model.addAttribute("user", user);
        model.addAttribute("balance", balanceService.getCurrentBalanceInDollars(user));

        Pageable pageable = PageRequest.of(0, 5);
        List<Transaction> recentTransactions = transactionService
                .getUserTransactionsPaginated(user, pageable)
                .stream()
                    .limit(5)
                .toList();

        // Convert recent transactions to BalancedTransaction objects
        List<BalancedTransaction> balancedTransactions = balanceService.balanceTransactions(recentTransactions);
        
        model.addAttribute("recentTransactions", balancedTransactions);
        return "ssr/dashboard";
    }
    
    @GetMapping("/transactions")
    public String getTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            Model model) {
        
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionsPage = transactionService.getUserTransactionsPaginated(user, pageable);
        
        // Convert Transaction objects to BalancedTransaction objects
        List<BalancedTransaction> balancedTransactions = balanceService.balanceTransactions(transactionsPage.getContent());

        model.addAttribute("user", user);
        model.addAttribute("transactions", balancedTransactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionsPage.getTotalPages());
        
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
            BigDecimal currentBalance = balanceService.getCurrentBalanceInDollars(user);
            
            // Check for self-transfer
            if (user.getUsername().equals(recipientUsername)) {
                model.addAttribute("error", "Cannot transfer funds to yourself");
                return "ssr/transfer";
            }
            
            // Check for sufficient funds
            if (currentBalance.compareTo(amount) < 0) {
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
            BigDecimal newBalance = currentBalance.subtract(amount);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Transfer successful. Your new balance is $" + newBalance);
            
            // For HTMX requests, return a fragment with the new balance
            if (isHtmxRequest()) {
                model.addAttribute("balance", newBalance);
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
