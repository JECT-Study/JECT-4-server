package com.ject.studytrip.trip.application.service

import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.trip.domain.factory.TripReportStudyLogFactory
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogCommandRepository
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogRepository
import org.springframework.stereotype.Service

@Service
class TripReportStudyLogCommandService(
    private val tripReportStudyLogRepository: TripReportStudyLogRepository,
    private val tripReportStudyLogCommandRepository: TripReportStudyLogCommandRepository,
) {
    fun createTripReportStudyLogs(
        tripReport: TripReport,
        studyLogs: List<StudyLog>,
    ) {
        tripReportStudyLogRepository.saveAll(studyLogs.map { TripReportStudyLogFactory.create(tripReport, it) })
    }

    fun hardDeleteTripReportStudyLogsOwnedByDeletedMember(): Long = tripReportStudyLogCommandRepository.deleteAllByDeletedMemberOwner()

    fun hardDeleteTripReportStudyLogsOwnedByMember(memberId: Long): Long = tripReportStudyLogCommandRepository.deleteAllByMemberId(memberId)
}
