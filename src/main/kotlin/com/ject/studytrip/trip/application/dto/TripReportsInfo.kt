package com.ject.studytrip.trip.application.dto

data class TripReportsInfo(
    val tripReportInfos: List<TripReportInfo>,
) {
    companion object {
        @JvmStatic
        fun of(tripReportInfos: List<TripReportInfo>): TripReportsInfo = TripReportsInfo(tripReportInfos)
    }
}
