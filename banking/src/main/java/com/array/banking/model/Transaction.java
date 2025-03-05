package com.array.banking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    public Transaction(User user, BigDecimal amount, TransactionType type, BigDecimal balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
    }

    public Transaction(User user, BigDecimal amount, TransactionType type, BigDecimal balanceAfter, TransactionStatus status) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.status = status;
    }
}
