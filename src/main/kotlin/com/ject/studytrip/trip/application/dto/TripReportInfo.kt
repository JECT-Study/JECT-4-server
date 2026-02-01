package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.trip.domain.model.TripReport

data class TripReportInfo(
    val tripReportId: Long,
    val title: String,
    val content: String,
    val startDate: String,
    val endDate: String,
    val studyLogCount: Long,
    val totalFocusHours: Long,
    val studyDays: Long,
    val imageTitle: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        @JvmStatic
        fun from(tripReport: TripReport): TripReportInfo =
            TripReportInfo(
                tripReport.id,
                tripReport.title,
                tripReport.content,
                tripReport.startDate,
                tripReport.endDate,
                tripReport.studyLogCount,
                tripReport.totalFocusHours,
                tripReport.studyDays,
                tripReport.imageTitle,
                tripReport.imageUrl,
                DateUtil.formatDateTime(tripReport.createdAt),
                DateUtil.formatDateTime(tripReport.updatedAt),
                tripReport.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
