package com.ject.studytrip.stamp.domain.repository;

import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;

public interface StampRepository {
    List<Stamp> saveAll(List<Stamp> stamps);

    List<Stamp> findAllByTripId(Long tripId);

    List<Stamp> findAllByTripIdOrderByDeadlineAsc(Long tripId);
}
