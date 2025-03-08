package com.array.banking.repository;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    List<Transaction> findByUser(User user);
        
    List<Transaction> findByUserAndType(User user, TransactionType type);
    
    List<Transaction> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    
    // Update method name to indicate secondary sort criterion
    List<Transaction> findByUserOrderByTimestampDescTransactionIdDesc(User user);
    
    // Update method to ensure consistent sorting with timestamp and transaction ID
    Page<Transaction> findByUserOrderByTimestampDescTransactionIdDesc(User user, Pageable pageable);
    
    // Calculate user balance in cents UP TO AND INCLUDING a specific transaction
    // TODO: Coalesce for null safety, since we have NOT NULL constraints on the columns we might be able to remove it
    @Query(value = """
            SELECT COALESCE(SUM(CASE 
                WHEN UPPER(transaction_type) IN ('DEPOSIT', 'TRANSFER_IN') THEN amount 
                WHEN UPPER(transaction_type) IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN -amount 
                ELSE 0 
            END), 0) 
            FROM transactions 
            WHERE user_id = :userId AND status = 'COMPLETED'
            AND (timestamp < (SELECT timestamp FROM transactions WHERE transaction_id = :transactionId)
                 OR (timestamp = (SELECT timestamp FROM transactions WHERE transaction_id = :transactionId) 
                     AND transaction_id <= :transactionId))
            """, nativeQuery = true)
    Long calculateBalanceAtTransaction(@Param("userId") Integer userId, @Param("transactionId") Integer transactionId);

    // Calculate user balance in cents from transactions
    // TODO: Coalesce for null safety, since we have NOT NULL constraints on the columns we might be able to remove it
    @Query(value = """
            SELECT COALESCE(SUM(CASE 
                WHEN UPPER(transaction_type) IN ('DEPOSIT', 'TRANSFER_IN') THEN amount 
                WHEN UPPER(transaction_type) IN ('WITHDRAWAL', 'TRANSFER_OUT') THEN -amount 
                ELSE 0 
            END), 0) 
            FROM transactions 
            WHERE user_id = :userId AND status = 'COMPLETED'
            """, nativeQuery = true)
    Long calculateBalanceForUser(@Param("userId") Integer userId);
    
    // Find the latest completed transaction for a user
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.status = 'COMPLETED' ORDER BY t.timestamp DESC, t.transactionId DESC")
    List<Transaction> findLatestCompletedTransactionByUser(@Param("userId") Integer userId, Pageable pageable);
    
    // Find transactions with specific status
    List<Transaction> findByUserAndStatusOrderByTimestampDescTransactionIdDesc(User user, TransactionStatus status);
}
