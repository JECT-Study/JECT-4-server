package com.ject.studytrip.pomodoro.domain.repository;

public interface PomodoroQueryRepository {
    long deleteAllByDeletedAtIsNotNull();

    long deleteAllByDeletedDailyGoalOwner();

    long sumFocusHoursByTripId(Long tripId);

    long deleteAllByMemberId(Long memberId);
}
