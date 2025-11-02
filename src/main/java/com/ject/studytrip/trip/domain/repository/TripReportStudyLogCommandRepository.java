package com.ject.studytrip.trip.domain.repository;

public interface TripReportStudyLogCommandRepository {
    long deleteAllByDeletedMemberOwner();

    long deleteAllByMemberId(Long memberId);
}
