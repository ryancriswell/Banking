package com.array.banking.controller;

import com.array.banking.dto.LoginRequest;
import com.array.banking.dto.RegisterRequest;
import com.array.banking.model.User;
import com.array.banking.security.JwtTokenProvider;
import com.array.banking.service.RandomTransactionService;
import com.array.banking.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/banking/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RandomTransactionService randomTransactionService;

    // TODO: response returns DTO
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        
        // Generate token
        String token = jwtTokenProvider.createToken(username);
        
        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        
        try {
            // Simulate random transactions for the user on login
            randomTransactionService.generateRandomTransactions(userService.getUserByUsername(username).get());
        } catch (Exception e) {
            log.error("Failed to generate random transactions", e);
            return ResponseEntity.internalServerError().body("Failed to generate random transactions");
        }
        
        return ResponseEntity.ok(response);
    }

    // TODO: response returns DTO
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String email = registerRequest.getEmail();
        
        User user = userService.createUser(username, password, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        
        return ResponseEntity.ok(response);
    }
}
