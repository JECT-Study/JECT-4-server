package com.ject.studytrip.trip.domain.repository

import com.ject.studytrip.trip.domain.model.TripReport
import java.util.Optional

interface TripReportRepository {
    fun findById(tripReportId: Long): Optional<TripReport>

    fun save(tripReport: TripReport): TripReport
}
