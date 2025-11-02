package com.ject.studytrip.studylog.domain.repository;

public interface StudyLogDailyMissionCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedDailyMissionOwner();

    long deleteAllByDeletedStudyLogOwner();

    long deleteAllByMemberId(Long memberId);
}
