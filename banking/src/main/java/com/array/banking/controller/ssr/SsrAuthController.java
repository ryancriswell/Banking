package com.array.banking.controller.ssr;

import com.array.banking.dto.LoginRequest;
import com.array.banking.dto.RegisterRequest;
import com.array.banking.model.User;
import com.array.banking.security.JwtTokenProvider;
import com.array.banking.service.RandomTransactionService;
import com.array.banking.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ssr")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SsrAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RandomTransactionService randomTransactionService;

    // TODO: Set the JWT cookie name in configuration
    private String jwtCookieName = "jwt_token";

    @GetMapping("/login")
    public String loginForm(Model model, HttpServletRequest request) {
        // Check if already authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            log.debug("User already authenticated, redirecting to dashboard");
            return "redirect:/ssr/dashboard";
        }
        
        // Handle login form display
        model.addAttribute("loginRequest", new LoginRequest());
        return "ssr/login";
    }
    
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest, 
                        BindingResult bindingResult, 
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (bindingResult.hasErrors()) {
            log.error("Login request has errors: {}", bindingResult.getAllErrors());
            return "ssr/login";
        }
        
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            
            log.debug("Attempting to authenticate user: {}", username);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT token
            String token = jwtTokenProvider.createToken(username);
            
            // Store token in a cookie
            Cookie jwtCookie = new Cookie(jwtCookieName, token);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(3600); // 1 hour
            response.addCookie(jwtCookie);
            
            log.debug("User authenticated successfully: {}, redirecting to dashboard", username);
            
            // Generate random transactions for demo purposes
            try {
                User user = userService.getUserByUsername(username).orElseThrow();
                randomTransactionService.generateRandomTransactions(user);
            } catch (Exception e) {
                log.error("Failed to generate random transactions", e);
            }
            
            // Use sendRedirect for more reliable redirection
            response.sendRedirect("/ssr/dashboard");
            return null; // Return null as we've manually handled the response
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            model.addAttribute("error", "Invalid username or password");
            return "ssr/login";
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred");
            return "ssr/login";
        }
    }
    
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "ssr/register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "ssr/register";
        }
        
        try {
            String username = registerRequest.getUsername();
            String password = registerRequest.getPassword();
            String email = registerRequest.getEmail();
            
            // Check if username already exists
            if (userService.getUserByUsername(username).isPresent()) {
                bindingResult.rejectValue("username", "error.username", "Username already taken");
                return "ssr/register";
            }
            
            userService.createUser(username, password, email);
            redirectAttributes.addFlashAttribute("message", "Registration successful. Please login.");
            return "redirect:/ssr/login";
        } catch (Exception e) {
            bindingResult.reject("error.global", "Registration failed: " + e.getMessage());
            return "ssr/register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie(jwtCookieName, null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        // Clear the security context
        SecurityContextHolder.clearContext();
        
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/ssr/login";
    }
}
