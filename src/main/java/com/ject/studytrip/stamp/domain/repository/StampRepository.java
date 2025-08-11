package com.ject.studytrip.stamp.domain.repository;

import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import java.util.Optional;

public interface StampRepository {
    Stamp save(Stamp stamp);

    List<Stamp> saveAll(List<Stamp> stamps);

    Optional<Stamp> findById(Long stampId);

    List<Stamp> findAllByIdIn(List<Long> ids);

    List<Stamp> findAllByTripIdAndDeletedAtIsNull(Long tripId);

    List<Stamp> findAllByTripIdOrderByCreatedAtAsc(Long tripId);
}
