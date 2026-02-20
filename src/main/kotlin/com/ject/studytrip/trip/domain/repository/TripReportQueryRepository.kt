package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.TripReport

interface TripReportQueryRepository {
    fun findAllActiveByMemberId(memberId: Long): List<TripReport>

    fun findImageUrlsByMemberId(memberId: Long): List<String>
}
