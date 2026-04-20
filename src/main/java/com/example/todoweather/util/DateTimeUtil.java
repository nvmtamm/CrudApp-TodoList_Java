package com.example.todoweather.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter ISO_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtil() {
        // Utility class, no instantiation
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(ISO_FORMAT);
    }

    public static boolean isWithinHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dateTime.isAfter(now) && dateTime.isBefore(now.plusHours(hours));
    }
}
