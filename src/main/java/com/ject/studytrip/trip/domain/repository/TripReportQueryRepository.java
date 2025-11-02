package com.ject.studytrip.trip.domain.repository;

import java.util.List;

public interface TripReportQueryRepository {
    List<String> findImageUrlsByMemberId(Long memberId);
}
