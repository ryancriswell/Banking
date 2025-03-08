package com.array.banking.dto;

import com.array.banking.model.Transaction;
import com.array.banking.model.TransactionStatus;
import com.array.banking.model.TransactionType;
import com.array.banking.util.CurrencyUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Integer transactionId;
    private BigDecimal amountInDollars;
    private TransactionType type;
    private LocalDateTime timestamp;
    private TransactionStatus status;
    
    /**
     * Create a TransactionResponseDto from a Transaction entity
     */
    public static TransactionResponse fromTransaction(Transaction transaction) {
        return new TransactionResponse(
            transaction.getTransactionId(),
            CurrencyUtil.centsToDollars(transaction.getAmount()),
            transaction.getType(),
            transaction.getTimestamp(),
            transaction.getStatus()
        );
    }
}
