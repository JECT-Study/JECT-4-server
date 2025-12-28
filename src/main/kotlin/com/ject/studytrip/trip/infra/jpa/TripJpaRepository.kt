package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.Trip
import org.springframework.data.jpa.repository.JpaRepository

interface TripJpaRepository : JpaRepository<Trip, Long>
