package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TripQueryRepository {
    Slice<Trip> findSliceByMemberId(Long memberId, Pageable pageable);
}
