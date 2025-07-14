package com.ject.studytrip.stamp.infra.jpa;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampRepositoryAdapter implements StampRepository {
    private final StampJpaRepository stampJpaRepository;

    @Override
    public Stamp save(Stamp stamp) {
        return stampJpaRepository.save(stamp);
    }

    @Override
    public List<Stamp> saveAll(List<Stamp> stamps) {
        return stampJpaRepository.saveAll(stamps);
    }

    @Override
    public Optional<Stamp> findById(Long stampId) {
        return stampJpaRepository.findById(stampId);
    }

    @Override
    public List<Stamp> findAllByIdIn(List<Long> ids) {
        return stampJpaRepository.findAllByIdIn(ids);
    }

    @Override
    public List<Stamp> findAllByTripIdAndDeletedAtIsNull(Long tripId) {
        return stampJpaRepository.findAllByTripIdAndDeletedAtIsNull(tripId);
    }

    @Override
    public List<Stamp> findAllByTripIdOrderByDeadlineAsc(Long tripId) {
        return stampJpaRepository.findAllByTripIdOrderByDeadlineAsc(tripId);
    }
}
