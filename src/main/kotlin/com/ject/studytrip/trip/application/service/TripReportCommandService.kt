package com.ject.studytrip.trip.application.service

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.factory.TripReportFactory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository
import com.ject.studytrip.trip.domain.repository.TripReportRepository
import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest
import org.springframework.stereotype.Service

@Service
class TripReportCommandService(
    private val tripReportRepository: TripReportRepository,
    private val tripReportCommandRepository: TripReportCommandRepository,
) {
    fun createTripReport(
        member: Member,
        request: CreateTripReportRequest,
    ): TripReport {
        val tripReport =
            TripReportFactory.create(
                member,
                request.title,
                request.content,
                request.startDate,
                request.endDate,
                request.studyLogCount,
                request.totalFocusHours,
                request.studyDays,
                request.imageTitle,
            )

        return tripReportRepository.save(tripReport)
    }

    fun updateImageUrl(
        tripReport: TripReport,
        imageUrl: String,
    ) = tripReport.updateImageUrl(imageUrl)

    fun deleteTripReport(tripReport: TripReport) = tripReport.updateDeletedAt()

    fun hardDeleteTripReports(): Long = tripReportCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteTripReportsOwnedByDeletedMember(): Long = tripReportCommandRepository.deleteAllByDeletedMemberOwner()

    fun hardDeleteTripReportsOwnedByMember(memberId: Long): Long = tripReportCommandRepository.deleteAllByMemberId(memberId)
}
