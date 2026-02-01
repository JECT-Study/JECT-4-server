package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.TripReport;
import java.util.List;

public interface TripReportQueryRepository {
    List<TripReport> findAllActiveByMemberId(Long memberId);

    List<String> findImageUrlsByMemberId(Long memberId);
}
