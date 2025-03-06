package com.array.banking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets;
    private final Supplier<Bucket> bucketSupplier;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting for static resources
        String requestURI = request.getRequestURI();
        if (requestURI.contains(".") || 
            requestURI.startsWith("/webjars") || 
            requestURI.startsWith("/css") || 
            requestURI.startsWith("/js")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get the bucket for this IP or create a new one
        String clientIp = getClientIP(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> bucketSupplier.get());

        // Try to consume 1 token
        if (bucket.tryConsume(1)) {
            // Request allowed, continue the filter chain
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            Map<String, Object> body = Map.of(
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", "You have exhausted your API request quota"
            );
            
            objectMapper.writeValue(response.getOutputStream(), body);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
