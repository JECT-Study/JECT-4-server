package com.ject.studytrip.studylog.domain.repository;

import com.ject.studytrip.studylog.domain.model.StudyLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StudyLogQueryRepository {
    Slice<StudyLog> findSliceByTripId(Long tripId, Pageable pageable, String order);

    Slice<StudyLog> findSliceByTripReportIdOrderByCreatedAtDesc(
            Long tripReportId, Pageable pageable);

    List<Long> findAllIdsByTripIdOrderByCreatedDesc(Long tripId);

    List<String> findImageUrlsByMemberId(Long memberId);

    long countStudyLogsByTripId(Long tripId);

    long countActiveStudyLogsByMemberId(Long memberId);
}
