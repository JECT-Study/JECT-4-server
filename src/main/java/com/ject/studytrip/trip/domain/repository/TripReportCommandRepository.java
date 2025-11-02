package com.ject.studytrip.trip.domain.repository;

public interface TripReportCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    long deleteAllByMemberId(Long memberId);
}
