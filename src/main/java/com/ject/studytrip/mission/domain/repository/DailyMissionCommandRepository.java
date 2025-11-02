package com.ject.studytrip.mission.domain.repository;

public interface DailyMissionCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedMissionOwner();

    long deleteAllByDeletedDailyGoalOwner();

    long deleteAllByMemberId(Long memberId);
}
