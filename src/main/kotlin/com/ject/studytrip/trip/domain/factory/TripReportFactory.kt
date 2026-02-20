package com.ject.studytrip.trip.domain.factory

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.model.TripReport

object TripReportFactory {
    fun create(
        member: Member,
        title: String,
        content: String,
        startDate: String,
        endDate: String?,
        studyLogCount: Long,
        totalFocusHours: Long,
        studyDays: Long,
        imageTitle: String?,
    ): TripReport = TripReport.of(member, title, content, startDate, endDate, studyLogCount, totalFocusHours, studyDays, imageTitle)
}
