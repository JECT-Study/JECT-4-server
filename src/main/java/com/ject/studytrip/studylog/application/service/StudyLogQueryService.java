package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.studylog.domain.error.StudyLogErrorCode;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.policy.StudyLogPolicy;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import java.util.List;
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

    public Slice<StudyLog> getStudyLogsSliceByTripId(
            Long tripId, int page, int size, String order) {
        return studyLogQueryRepository.findSliceByTripId(tripId, PageRequest.of(page, size), order);
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

    public List<StudyLog> getValidStudyLogs(List<Long> studyLogIds) {
        List<StudyLog> studyLogs = studyLogRepository.findAllByIdIn(studyLogIds);

        StudyLogPolicy.validateExistAll(studyLogs, studyLogIds);
        studyLogs.forEach(StudyLogPolicy::validateNotDeleted);

        return studyLogs;
    }

    public long getStudyLogCountByTripId(Long tripId) {
        return studyLogQueryRepository.countStudyLogsByTripId(tripId);
    }

    public Slice<StudyLog> getStudyLogsSliceByTripReportId(Long tripReportId, int page, int size) {
        return studyLogQueryRepository.findSliceByTripReportIdOrderByCreatedAtDesc(
                tripReportId, PageRequest.of(page, size));
    }
}
