package com.array.banking.config;

import com.array.banking.security.JwtTokenFilter;
import com.array.banking.security.JwtTokenProvider;
import com.array.banking.security.RateLimitingFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, RateLimitingFilter rateLimitingFilter) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/banking/v1/login", "/banking/v1/register").permitAll()
                // Swagger UI and OpenAPI endpoints
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", 
                                 "/webjars/**", "/swagger-ui/index.html").permitAll()
                // H2 Console
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            );
        
        // Add rate limiting filter first
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Add JWT token filter
        http.addFilterBefore(new JwtTokenFilter(jwtTokenProvider), 
                            UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
