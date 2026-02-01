package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.TripReport
import org.springframework.data.jpa.repository.JpaRepository

interface TripReportJpaRepository : JpaRepository<TripReport, Long>
