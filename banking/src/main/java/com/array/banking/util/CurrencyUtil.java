package com.array.banking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Format currency for for conversion between frontend requests and responses
// TODO: Add more currency conversion methods for supporting other currency types
public class CurrencyUtil {

    /**
     * Convert dollars and cents to cents (Long)
     * Example: 10.99 -> 1099
     */
    public static Long dollarsToCents(BigDecimal dollars) {
        if (dollars == null) return 0L;
        return Long.valueOf(dollars.multiply(BigDecimal.valueOf(100)).intValue());
    }
    
    /**
     * Convert cents to dollars (BigDecimal) rounding down
     * Example: 1099 -> 10.99
     */
    public static BigDecimal centsToDollars(Long cents) {
        if (cents == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
    }
}
