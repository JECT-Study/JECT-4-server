package com.ject.studytrip.studylog.domain.repository

import com.ject.studytrip.studylog.domain.model.StudyLog
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface StudyLogQueryRepository {
    fun findSliceByTripId(
        tripId: Long,
        pageable: Pageable,
        order: String,
    ): Slice<StudyLog>

    fun findSliceByTripReportIdOrderByCreatedAtDesc(
        tripReportId: Long,
        pageable: Pageable,
    ): Slice<StudyLog>

    fun findAllIdsByTripIdOrderByCreatedDesc(tripId: Long): List<Long>

    fun findImageUrlsByMemberId(memberId: Long): List<String>

    fun countStudyLogsByTripId(tripId: Long): Long

    fun countActiveStudyLogsByMemberId(memberId: Long): Long
}
