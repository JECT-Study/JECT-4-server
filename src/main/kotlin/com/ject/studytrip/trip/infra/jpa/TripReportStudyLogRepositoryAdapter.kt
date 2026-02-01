package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.TripReportStudyLog
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository
import org.springframework.stereotype.Repository

@Repository
class TripReportStudyLogRepositoryAdapter(
    private val tripReportStudyLogJpaRepository: TripReportStudyLogJpaRepository,
) : TripReportStudyLogRepository {
    override fun saveAll(tripReportStudyLogs: List<TripReportStudyLog>) {
        tripReportStudyLogJpaRepository.saveAll(tripReportStudyLogs)
    }
}
