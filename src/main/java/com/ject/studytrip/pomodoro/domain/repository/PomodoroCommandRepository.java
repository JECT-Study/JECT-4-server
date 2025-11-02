package com.ject.studytrip.pomodoro.domain.repository;

public interface PomodoroCommandRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedDailyGoalOwner();

    long deleteAllByMemberId(Long memberId);
}
