package com.array.banking.controller;

import com.array.banking.dto.LoginRequest;
import com.array.banking.dto.LoginResponse;
import com.array.banking.dto.RegisterRequest;
import com.array.banking.dto.RegisterResponse;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        
        // Generate token
        String token = jwtTokenProvider.createToken(username);
        
        // Create response
        LoginResponse response = new LoginResponse(token, username);
        
        try {
            // Simulate random transactions for the user on login
            randomTransactionService.generateRandomTransactions(userService.getUserByUsername(username).get());
        } catch (Exception e) {
            log.error("Failed to generate random transactions", e);
            return ResponseEntity.internalServerError().body("Failed to generate random transactions");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String email = registerRequest.getEmail();
        
        User user = userService.createUser(username, password, email);
        
        RegisterResponse response = new RegisterResponse(
            user.getUserId(),
            user.getUsername(),
            user.getEmail()
        );
        
        return ResponseEntity.ok(response);
    }
}
