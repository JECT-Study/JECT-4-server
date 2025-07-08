package com.ject.studytrip.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DEFAULT_DATE_FORMATTER) : null;
    }

    public static LocalDate parseDate(String raw) {
        return LocalDate.parse(raw, DEFAULT_DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_DATETIME_FORMATTER) : null;
    }

    public static LocalDateTime parseDateTime(String raw) {
        return LocalDateTime.parse(raw, DEFAULT_DATETIME_FORMATTER);
    }
}
