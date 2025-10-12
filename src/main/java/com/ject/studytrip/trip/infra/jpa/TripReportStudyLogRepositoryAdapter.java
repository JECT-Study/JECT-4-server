package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.TripReportStudyLog;
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportStudyLogRepositoryAdapter implements TripReportStudyLogRepository {
    private final TripReportStudyLogJpaRepository tripReportStudyLogJpaRepository;

    @Override
    public void saveAll(List<TripReportStudyLog> tripReportStudyLogs) {
        tripReportStudyLogJpaRepository.saveAll(tripReportStudyLogs);
    }
}
