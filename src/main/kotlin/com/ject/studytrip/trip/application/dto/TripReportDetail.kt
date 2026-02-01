package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo

class TripReportDetail(
    val tripReportInfo: TripReportInfo,
    val studyLogSliceInfo: StudyLogSliceInfo,
) {
    companion object {
        @JvmStatic
        fun from(
            tripReportInfo: TripReportInfo,
            studyLogSliceInfo: StudyLogSliceInfo,
        ) = TripReportDetail(tripReportInfo, studyLogSliceInfo)
    }
}
