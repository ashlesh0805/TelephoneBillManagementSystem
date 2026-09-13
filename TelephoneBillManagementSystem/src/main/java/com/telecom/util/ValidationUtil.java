package com.telecom.util;

import java.util.regex.Pattern;

/**
 * Utility for input validation across customer registration, phone entries, and tariffs.
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{7,15}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return true; // Optional field
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        String clean = phone.replaceAll("[\\s\\-()]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    public static boolean isPositiveNumber(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            double v = Double.parseDouble(value.trim());
            return v >= 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            int v = Integer.parseInt(value.trim());
            return v >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
