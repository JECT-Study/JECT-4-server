package com.ject.studytrip.trip.application.dto

data class PresignedTripReportImageInfo(
    val tripReportId: Long,
    val tmpKey: String,
    val presignedUrl: String,
)
