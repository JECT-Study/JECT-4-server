package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripReportStudyLogJpaRepository extends JpaRepository<TripReportStudyLog, Long> {}
