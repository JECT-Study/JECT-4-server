package com.ject.studytrip.trip.domain.repository;

public interface TripReportStudyLogQueryRepository {
    long deleteAllByDeletedMemberOwner();

    long deleteAllByMemberId(Long memberId);
}
