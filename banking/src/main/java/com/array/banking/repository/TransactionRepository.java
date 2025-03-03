package com.array.banking.repository;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    List<Transaction> findByUser(User user);
        
    List<Transaction> findByUserAndType(User user, TransactionType type);
    
    List<Transaction> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    
    List<Transaction> findByUserOrderByTimestampDesc(User user);
    
    // Add paginated query method
    Page<Transaction> findByUserOrderByTimestampDesc(User user, Pageable pageable);
}
