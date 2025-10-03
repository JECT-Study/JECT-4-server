package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.policy.StudyLogPolicy;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyLogQueryService {
    private final StudyLogRepository studyLogRepository;
    private final StudyLogQueryRepository studyLogQueryRepository;

    public long getActiveStudyLogCountByMemberId(Long memberId) {
        return studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId);
    }

    public Slice<StudyLog> getStudyLogsSliceByTripId(Long tripId, int page, int size) {
        return studyLogQueryRepository.findSliceByTripIdOrderByCreatedAtDesc(
                tripId, PageRequest.of(page, size));
    }

    public StudyLog getValidStudyLog(Long studyLogId) {
        StudyLog studyLog =
                studyLogRepository
                        .findById(studyLogId)
                        .orElseThrow(
                                () -> new CustomException(StudyLogErrorCode.STUDY_LOG_NOT_FOUND));

        StudyLogPolicy.validateNotDeleted(studyLog);

        return studyLog;
    }
}
