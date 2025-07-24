package com.ject.studytrip.studylog.domain.repository;

public interface StudyLogQueryRepository {
    long countActiveStudyLogsByMemberId(Long memberId);
}
