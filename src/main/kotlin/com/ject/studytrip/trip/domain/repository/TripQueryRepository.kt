package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface TripQueryRepository {
    fun findSliceByMemberIdAndCompletedFalseAndDeletedAtIsNull(
        memberId: Long,
        pageable: Pageable,
    ): Slice<Trip>

    fun countActiveTripsByMemberIdAndCategory(
        memberId: Long,
        category: TripCategory,
    ): Long
}
