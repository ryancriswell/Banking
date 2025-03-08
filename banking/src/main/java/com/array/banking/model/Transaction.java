package com.array.banking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    
    // Should user transactions really be deleted? Maybe we should keep them for auditing
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "amount", nullable = false)
    private Long amount; // Amount in cents
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    public Transaction(User user, Long amountInCents, TransactionType type) {
        this.user = user;
        this.amount = amountInCents;
        this.type = type;
    }

    public Transaction(User user, Long amountInCents, TransactionType type, TransactionStatus status) {
        this.user = user;
        this.amount = amountInCents;
        this.type = type;
        this.status = status;
    }
}
