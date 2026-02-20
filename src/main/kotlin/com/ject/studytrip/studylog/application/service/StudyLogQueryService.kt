package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.policy.StudyLogPolicy
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class StudyLogQueryService(
    private val studyLogRepository: StudyLogRepository,
    private val studyLogQueryRepository: StudyLogQueryRepository,
) {
    fun getActiveStudyLogCountByMemberId(memberId: Long): Long = studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId)

    fun getStudyLogsSliceByTripId(
        tripId: Long,
        page: Int,
        size: Int,
        order: String,
    ): Slice<StudyLog> = studyLogQueryRepository.findSliceByTripId(tripId, PageRequest.of(page, size), order)

    fun getValidStudyLog(studyLogId: Long): StudyLog {
        val studyLog =
            studyLogRepository
                .findById(studyLogId)
                .orElseThrow { CustomException(StudyLogErrorCode.STUDY_LOG_NOT_FOUND) }

        StudyLogPolicy.validateNotDeleted(studyLog)

        return studyLog
    }

    fun getValidStudyLogs(studyLogIds: List<Long>): List<StudyLog> {
        val studyLogs = studyLogRepository.findAllByIdIn(studyLogIds)

        StudyLogPolicy.validateExistAll(studyLogs, studyLogIds)
        studyLogs.forEach { StudyLogPolicy.validateNotDeleted(it) }

        return studyLogs
    }

    fun getStudyLogCountByTripId(tripId: Long): Long = studyLogQueryRepository.countStudyLogsByTripId(tripId)

    fun getStudyLogsSliceByTripReportId(
        tripReportId: Long,
        page: Int,
        size: Int,
    ): Slice<StudyLog> = studyLogQueryRepository.findSliceByTripReportIdOrderByCreatedAtDesc(tripReportId, PageRequest.of(page, size))

    fun getStudyLogIdsByTripId(tripId: Long): List<Long> = studyLogQueryRepository.findAllIdsByTripIdOrderByCreatedDesc(tripId)

    fun getStudyLogImageUrlsByMemberId(memberId: Long): List<String> = studyLogQueryRepository.findImageUrlsByMemberId(memberId)
}
