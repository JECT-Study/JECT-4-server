package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StudyLogQueryRepository {
    long countActiveStudyLogsByMemberId(Long memberId);

    Slice<StudyLog> findSliceByTripIdOrderByCreatedAtDesc(Long tripId, Pageable pageable);

    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    long deleteAllByDeletedDailyGoalOwner();

    Slice<StudyLog> findSliceByTripReportIdOrderByCreatedAtDesc(
            Long tripReportId, Pageable pageable);
}
