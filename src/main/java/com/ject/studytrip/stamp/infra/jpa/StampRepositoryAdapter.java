package com.ject.studytrip.stamp.infra.jpa;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampRepositoryAdapter implements StampRepository {
    private final StampJpaRepository stampJpaRepository;

    @Override
    public List<Stamp> saveAll(List<Stamp> stamps) {
        return stampJpaRepository.saveAll(stamps);
    }

    @Override
    public List<Stamp> findAllByTripId(Long tripId) {
        return stampJpaRepository.findAllByTripId(tripId);
    }

    @Override
    public List<Stamp> findAllByTripIdOrderByDeadlineAsc(Long tripId) {
        return stampJpaRepository.findAllByTripIdOrderByDeadlineAsc(tripId);
    }
}
