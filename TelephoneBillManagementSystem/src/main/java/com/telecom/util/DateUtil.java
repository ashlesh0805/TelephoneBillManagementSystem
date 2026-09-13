package com.telecom.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for date/time formatting and parsing.
 */
public class DateUtil {
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return "";
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FMT);
            return date.format(DISPLAY_DATE_FMT);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }

    public static String formatDisplayDateTime(String dtStr) {
        if (dtStr == null || dtStr.isBlank()) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(dtStr.trim(), DATETIME_FMT);
            return dt.format(DISPLAY_DATETIME_FMT);
        } catch (DateTimeParseException e) {
            return dtStr;
        }
    }

    public static String nowDateTimeString() {
        return LocalDateTime.now().format(DATETIME_FMT);
    }

    public static String nowDateString() {
        return LocalDate.now().format(DATE_FMT);
    }
}
