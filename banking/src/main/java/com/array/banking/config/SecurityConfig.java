package com.array.banking.config;

import com.array.banking.security.JwtTokenFilter;
import com.array.banking.security.JwtTokenProvider;
import com.array.banking.security.RateLimitingFilter;
import com.array.banking.security.SsrJwtCookieFilter;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          // Completely disable HTTP Basic authentication
          .httpBasic(basic -> basic.disable())
          // Disable form login to prevent default login pages/popups
          .formLogin(form -> form.disable())
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
              // Public endpoints
              .requestMatchers("/banking/v1/auth/**").permitAll()
              // SSR public endpoints
              .requestMatchers("/ssr/login", "/ssr/register", "/ssr/auth/login", "/ssr/auth/register").permitAll()
              // Forward SPA endpoints
              .requestMatchers("/spa/**", "/").permitAll()
              // Error endpoints
              .requestMatchers("/error").permitAll()
              // Static resources
              .requestMatchers("/css/**", "/js/**", "/webjars/**", "/images/**", "/*.ico").permitAll()
              // Swagger UI and OpenAPI endpoints
              .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                  "/webjars/**", "/swagger-ui/index.html")
              .permitAll()
              // H2 Console
              .requestMatchers("/h2-console/**").permitAll()
              .anyRequest().authenticated());

      // Add JWT cookie authentication filter first for SSR pages (before rate limiting)
      http.addFilterBefore(new SsrJwtCookieFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
      
      // Add rate limiting filter
      http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

      // Add JWT token filter for API requests
      http.addFilterBefore(new JwtTokenFilter(jwtTokenProvider),
          UsernamePasswordAuthenticationFilter.class);

      return http.build();
    }
    
    private CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
      configuration.setAllowCredentials(true); // Important for cookies
      
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
    }
}
