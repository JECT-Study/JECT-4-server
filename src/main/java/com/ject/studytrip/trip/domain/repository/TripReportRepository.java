package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.TripReport;
import java.util.List;
import java.util.Optional;

public interface TripReportRepository {
    Optional<TripReport> findById(Long tripReportId);

    List<TripReport> findAllByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

    TripReport save(TripReport tripReport);
}
