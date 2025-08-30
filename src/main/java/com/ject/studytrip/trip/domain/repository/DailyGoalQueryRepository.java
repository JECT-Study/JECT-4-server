package com.ject.studytrip.trip.domain.repository;

public interface DailyGoalQueryRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedTripOwner();
}
