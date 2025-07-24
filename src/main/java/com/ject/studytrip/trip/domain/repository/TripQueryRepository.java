package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TripQueryRepository {
    Slice<Trip> findSliceByMemberId(Long memberId, Pageable pageable);

    long countActiveTripsByMemberIdAndCategory(Long memberId, TripCategory category);
}
