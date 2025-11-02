package com.ject.studytrip.pomodoro.domain.repository;

public interface PomodoroQueryRepository {
    long sumFocusHoursByTripId(Long tripId);
}
