package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StudyLogQueryRepository {
    long countActiveStudyLogsByMemberId(Long memberId);

    Slice<StudyLog> findSliceByTripId(Long tripId, Pageable pageable, String order);

    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    long deleteAllByDeletedDailyGoalOwner();

    long countStudyLogsByTripId(Long tripId);

    Slice<StudyLog> findSliceByTripReportIdOrderByCreatedAtDesc(
            Long tripReportId, Pageable pageable);
}
