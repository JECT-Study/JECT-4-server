package com.ject.studytrip.trip.domain.repository;

public interface DailyGoalCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedTripOwner();

    long deleteAllByMemberId(Long memberId);
}
