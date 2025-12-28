package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.Trip
import java.util.Optional

interface TripRepository {
    fun findById(tripId: Long): Optional<Trip>

    fun save(trip: Trip): Trip
}
