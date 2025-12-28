package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.repository.TripRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class TripRepositoryAdapter(
    private val tripJpaRepository: TripJpaRepository,
) : TripRepository {
    override fun findById(tripId: Long): Optional<Trip> = tripJpaRepository.findById(tripId)

    override fun save(trip: Trip): Trip = tripJpaRepository.save(trip)
}
