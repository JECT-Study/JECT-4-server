package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.TripReportStudyLog

interface TripReportStudyLogRepository {
    fun saveAll(tripReportStudyLogs: List<TripReportStudyLog>)
}
