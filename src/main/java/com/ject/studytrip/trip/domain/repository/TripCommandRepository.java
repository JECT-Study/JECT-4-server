package com.ject.studytrip.trip.domain.repository;

public interface TripCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    long deleteAllByMemberId(Long memberId);
}
