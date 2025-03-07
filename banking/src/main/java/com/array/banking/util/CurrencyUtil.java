package com.array.banking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

// Format currency for the frontend responses
public class CurrencyUtil {

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

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
