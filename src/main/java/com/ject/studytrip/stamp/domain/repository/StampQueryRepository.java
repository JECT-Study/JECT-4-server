package com.ject.studytrip.stamp.domain.repository;

import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;
import java.util.Optional;

public interface StampQueryRepository {
    List<Stamp> findStampsToShiftAfterOrder(Long tripId, int deletedOrder);

    Optional<Stamp> findFirstIncompleteStampByTripId(Long tripId);
}
