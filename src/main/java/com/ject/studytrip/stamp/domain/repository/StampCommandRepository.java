package com.ject.studytrip.stamp.domain.repository;

public interface StampCommandRepository {
    boolean existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(Long tripId);

    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedTripOwner();

    long deleteAllByMemberId(Long memberId);
}
