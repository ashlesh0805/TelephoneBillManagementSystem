package com.telecom.util;

import java.text.DecimalFormat;

/**
 * Utility for currency formatting.
 */
public class CurrencyUtil {
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");
    private static final DecimalFormat COMPACT_FORMAT = new DecimalFormat("₹#,##0");

    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatCompact(double amount) {
        return COMPACT_FORMAT.format(amount);
    }

    public static double parse(String text) {
        if (text == null) return 0.0;
        String clean = text.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
