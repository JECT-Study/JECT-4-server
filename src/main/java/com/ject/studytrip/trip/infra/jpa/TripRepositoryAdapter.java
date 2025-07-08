package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripRepositoryAdapter implements TripRepository {
    private final TripJpaRepository tripJpaRepository;

    @Override
    public Optional<Trip> findById(Long id) {
        return tripJpaRepository.findById(id);
    }

    @Override
    public Trip save(Trip trip) {
        return tripJpaRepository.save(trip);
    }
}
