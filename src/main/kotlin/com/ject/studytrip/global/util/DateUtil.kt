package com.ject.studytrip.global.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtil {
    private val DEFAULT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE // yyyy-MM-dd

    private val DEFAULT_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun formatDate(date: LocalDate): String = date.format(DEFAULT_DATE_FORMATTER)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(DEFAULT_DATETIME_FORMATTER)
}
