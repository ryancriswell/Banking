package com.array.banking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsrJwtCookieFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    
    // TODO: Set the JWT cookie name in configuration
    private String jwtCookieName = "jwt_token";

    public SsrJwtCookieFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Only process SSR paths
        String path = request.getRequestURI();
        
        // Skip authentication for login and register pages
        if (path.startsWith("/ssr") && !isPublicSsrPath(path) && !isStaticResource(path)) {
            log.debug("Processing secured SSR path: {}", path);
            
            // Try to get JWT from cookie
            Optional<Cookie> jwtCookie = getJwtCookie(request);
            
            if (jwtCookie.isPresent()) {
                String token = jwtCookie.get().getValue();
                log.debug("Found JWT cookie for path: {}", path);
                
                try {
                    if (token != null && jwtTokenProvider.validateToken(token)) {
                        Authentication auth = jwtTokenProvider.getAuthentication(token);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("User authenticated successfully for path: {}", path);
                    } else {
                        log.debug("Invalid token for path: {}", path);
                        handleAuthenticationFailure(response);
                        return;
                    }
                } catch (Exception e) {
                    log.error("Error validating token for path: {}", path, e);
                    handleAuthenticationFailure(response);
                    return;
                }
            } else {
                log.debug("No JWT cookie found for path: {}", path);
                handleAuthenticationFailure(response);
                return;
            }
        } else {
            log.debug("Skipping authentication for public path: {}", path);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicSsrPath(String path) {
        return path.startsWith("/ssr/login") || 
               path.startsWith("/ssr/register");
    }
    
    private boolean isStaticResource(String path) {
        return path.endsWith(".css") || 
               path.endsWith(".js") || 
               path.endsWith(".ico") || 
               path.endsWith(".png") || 
               path.endsWith(".jpg") || 
               path.endsWith(".svg") || 
               path.startsWith("/webjars/");
    }
    
    private void handleAuthenticationFailure(HttpServletResponse response) throws IOException {
        log.debug("Redirecting to login page due to authentication failure");
        response.sendRedirect("/ssr/login");
    }
    
    private Optional<Cookie> getJwtCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .findFirst();
        }
        return Optional.empty();
    }
}
