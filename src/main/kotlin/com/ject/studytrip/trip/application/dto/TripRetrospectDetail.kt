package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo

data class TripRetrospectDetail(
    val summary: TripRetrospectSummary,
    val tripInfo: TripInfo,
    val studyLogSliceInfo: StudyLogSliceInfo,
)
