package com.array.banking.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.capacity}")
    private int capacity;

    @Value("${rate-limit.refill.tokens}")
    private int refillTokens;

    @Value("${rate-limit.refill.duration}")
    private int refillDuration;

    /**
     * Cache to store rate limiters by IP address
     */
    @Bean
    public Map<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Factory method to create a supplier of rate limiter buckets
     */
    @Bean
    public Supplier<Bucket> bucketSupplier() {
        return () -> {
            Bandwidth limit = Bandwidth.classic(capacity, 
                    Refill.intervally(refillTokens, Duration.ofMinutes(refillDuration)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        };
    }
}
