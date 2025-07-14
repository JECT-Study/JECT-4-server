package com.ject.studytrip.stamp.domain.repository;

import com.ject.studytrip.stamp.domain.model.Stamp;
import java.util.List;

public interface StampQueryRepository {
    List<Stamp> findStampsToShiftAfterOrder(Long tripId, int deletedOrder);
}
