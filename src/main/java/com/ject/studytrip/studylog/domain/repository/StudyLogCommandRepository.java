package com.ject.studytrip.studylog.domain.repository;

public interface StudyLogCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMemberOwner();

    long deleteAllByDeletedDailyGoalOwner();

    long deleteByMemberId(Long memberId);
}
