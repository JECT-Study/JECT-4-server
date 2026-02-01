package com.ject.studytrip.trip.domain.factory

import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.model.TripReportStudyLog

object TripReportStudyLogFactory {
    @JvmStatic
    fun create(
        tripReport: TripReport,
        studyLog: StudyLog,
    ): TripReportStudyLog = TripReportStudyLog.of(tripReport, studyLog)
}
