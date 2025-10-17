package com.ject.studytrip.trip.domain.repository;

public interface TripReportQueryRepository {
    long deleteAllByDeletedMemberOwner();
}
