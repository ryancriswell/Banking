package com.array.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private String message;
    private BigDecimal newBalance;
    private Integer transactionId;
}
