package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.studylog.domain.factory.StudyLogFactory;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyLogService {
    private final StudyLogRepository studyLogRepository;
    private final StudyLogQueryRepository studyLogQueryRepository;

    @Transactional(readOnly = true)
    public long getActiveStudyLogCountByMemberId(Long memberId) {
        return studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId);
    }

    public StudyLog createStudyLog(
            Member member, DailyGoal dailyGoal, String title, String content) {
        StudyLog studyLog = StudyLogFactory.create(member, dailyGoal, title, content);
        return studyLogRepository.save(studyLog);
    }

    public Slice<StudyLog> getStudyLogsSliceByTripId(Long tripId, int page, int size) {
        return studyLogQueryRepository.findSliceByTripIdOrderByCreatedAtDesc(
                tripId, PageRequest.of(page, size));
    }
}
