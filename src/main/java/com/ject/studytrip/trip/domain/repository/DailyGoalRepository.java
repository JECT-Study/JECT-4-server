package com.ject.studytrip.trip.domain.repository;

import com.ject.studytrip.trip.domain.model.DailyGoal;
import java.util.List;
import java.util.Optional;

public interface DailyGoalRepository {

    DailyGoal save(DailyGoal dailyGoal);

    Optional<DailyGoal> findById(Long id);

    List<DailyGoal> findAllByTripIdAndCompletedIsTrue(Long tripId);
}
