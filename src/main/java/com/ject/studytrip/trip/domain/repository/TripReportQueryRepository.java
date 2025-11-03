package com.ject.studytrip.trip.domain.repository;

import java.util.List;

public interface TripReportQueryRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    List<String> findImageUrlsByMemberId(Long memberId);

    long deleteAllByMemberId(Long memberId);
}
