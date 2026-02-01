package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class TripReportRepositoryAdapter(
    private val tripReportJpaRepository: TripReportJpaRepository,
) : TripReportRepository {
    override fun findById(tripReportId: Long): Optional<TripReport> = tripReportJpaRepository.findById(tripReportId)

    override fun save(tripReport: TripReport): TripReport = tripReportJpaRepository.save(tripReport)
}
