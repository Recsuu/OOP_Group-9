package service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * =============================================================
 * SERVICE CLASS: DateParser
 * =============================================================
 * PURPOSE
 * Utility class used to convert date and time strings
 * from CSV files into Java LocalDate and LocalTime objects.
 *
 * FUNCTIONS
 * - Parse date strings
 * - Parse time strings
 * - Format date for CSV storage
 */

public class DateParser {

    // Date format used in CSV
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    // Time format used in CSV
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    public static LocalDate parseDateMDY(String s) {
        return LocalDate.parse(s.trim(), DATE_FMT);
    }

    public static LocalTime parseTimeHM(String s) {
        return LocalTime.parse(s.trim(), TIME_FMT);
    }

    public static String formatDateMDY(LocalDate d) {
        return d.format(DATE_FMT);
    }
}