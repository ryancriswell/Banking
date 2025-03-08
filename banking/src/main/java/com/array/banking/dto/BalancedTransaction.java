package com.array.banking.dto;

import java.math.BigDecimal;

import com.array.banking.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalancedTransaction {
  private TransactionResponse transaction;
  private BigDecimal balanceAfter; // user's balance after completing this transaction

  
  /**
   * Create a BalancedTransaction from a Transaction entity and balance
   */
  public static BalancedTransaction fromTransaction(Transaction transaction, BigDecimal balance) {
    return new BalancedTransaction(
        TransactionResponse.fromTransaction(transaction),
        balance
    );
  }
}
