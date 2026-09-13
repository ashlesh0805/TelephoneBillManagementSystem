package com.telecom.service;

import com.telecom.model.CallType;

/**
 * Service for call prefix classification and duration calculations.
 */
public class CallService {

    /**
     * Auto-detects call type based on destination telephone number format and prefix.
     *
     * Rules:
     * - Starts with '+' or '00' (excluding '+91' India code) -> ISD (International)
     * - Starts with '0' (domestic STD trunk prefix, e.g. 022, 080, 011) -> STD (National Long Distance)
     * - Starts with '+91' or standard 10-digit mobile number -> LOCAL
     */
    public static CallType detectCallType(String destinationNumber) {
        if (destinationNumber == null) {
            return CallType.LOCAL;
        }

        String cleaned = destinationNumber.replaceAll("[\\s\\-()]", "");

        // International Call Detection
        if (cleaned.startsWith("+")) {
            if (cleaned.startsWith("+91")) {
                // Indian national number
                return CallType.LOCAL;
            }
            return CallType.ISD;
        }

        if (cleaned.startsWith("00")) {
            if (cleaned.startsWith("0091")) {
                return CallType.LOCAL;
            }
            return CallType.ISD;
        }

        // STD National Long Distance
        if (cleaned.startsWith("0") && cleaned.length() > 5) {
            return CallType.STD;
        }

        // Default to LOCAL
        return CallType.LOCAL;
    }

    /**
     * Validates that destination telephone number contains valid digits and symbols.
     */
    public static boolean isValidPhoneNumber(String number) {
        if (number == null) return false;
        String cleaned = number.replaceAll("[\\s\\-()]", "");
        return cleaned.matches("^\\+?[0-9]{7,15}$");
    }
}
