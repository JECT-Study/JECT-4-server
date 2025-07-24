package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyLogService {
    private final StudyLogQueryRepository studyLogQueryRepository;

    @Transactional(readOnly = true)
    public long getActiveStudyLogCountByMemberId(Long memberId) {
        return studyLogQueryRepository.countActiveStudyLogsByMemberId(memberId);
    }
}
