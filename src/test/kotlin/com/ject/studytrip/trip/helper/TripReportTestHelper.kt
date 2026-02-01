package com.ject.studytrip.trip.helper

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import com.ject.studytrip.trip.fixture.TripReportFixture
import org.springframework.stereotype.Component

@Component
class TripReportTestHelper(
    private val tripReportRepository: TripReportRepository,
) {
    fun saveTripReport(member: Member): TripReport = tripReportRepository.save(TripReportFixture(member).create())

    fun saveDeletedTripReport(member: Member): TripReport =
        tripReportRepository.save(
            TripReportFixture(member).create().also {
                it.updateDeletedAt()
            },
        )
}
