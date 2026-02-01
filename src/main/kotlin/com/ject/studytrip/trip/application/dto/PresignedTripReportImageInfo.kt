package com.ject.studytrip.trip.application.dto

data class PresignedTripReportImageInfo(
    val tripReportId: Long,
    val tmpKey: String,
    val presignedUrl: String,
) {
    companion object {
        @JvmStatic
        fun of(
            tripReportId: Long,
            tmpKey: String,
            presignedUrl: String,
        ): PresignedTripReportImageInfo = PresignedTripReportImageInfo(tripReportId, tmpKey, presignedUrl)
    }
}
