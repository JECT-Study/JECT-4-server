package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.Trip;
import java.util.Optional;

public interface TripRepository {
    Optional<Trip> findById(Long id);

    Trip save(Trip trip);
}
