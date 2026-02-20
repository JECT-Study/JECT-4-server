package com.ject.studytrip.stamp.domain.factory

import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.trip.domain.model.Trip
import java.time.LocalDate

object StampFactory {
    fun create(
        trip: Trip,
        name: String,
        stampOrder: Int,
        endDate: LocalDate?,
    ): Stamp = Stamp.of(trip, name, stampOrder, endDate)
}
