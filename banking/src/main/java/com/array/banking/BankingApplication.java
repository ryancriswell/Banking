package com.array.banking;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.array.banking.repository")
@EntityScan(basePackages = "com.array.banking.model")
public class BankingApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}

