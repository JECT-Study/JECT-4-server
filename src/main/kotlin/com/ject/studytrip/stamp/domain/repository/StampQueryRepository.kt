package com.ject.studytrip.stamp.domain.repository

import com.ject.studytrip.stamp.domain.model.Stamp
import java.util.Optional

interface StampQueryRepository {
    fun findStampsToShiftAfterOrder(
        tripId: Long,
        deletedOrder: Int,
    ): List<Stamp>

    fun findFirstIncompleteStampByTripId(tripId: Long): Optional<Stamp>

    fun findNextStampOrderByTripId(tripId: Long): Int
}
