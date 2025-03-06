package com.array.banking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class IndexController {
    
    @Value("${react-app.url}")
    private String reactAppUrl;
    
    /**
     * Redirect SPA routes to the React application
     * This preserves the original path when redirecting
     */
    @GetMapping(value = {
        "/",
        "/spa",
        "/spa/**"
    })
    public void redirectToReactApp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the full request URI
        String path = request.getRequestURI();
        
        // Build the target URL with the same path
        String targetUrl = reactAppUrl + path;
        
        // Add query parameters if present
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }
        
        // Perform the redirect
        response.sendRedirect(targetUrl);
    }
}
