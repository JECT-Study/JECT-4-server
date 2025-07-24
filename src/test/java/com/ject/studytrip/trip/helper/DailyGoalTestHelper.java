package com.ject.studytrip.trip.helper;

import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import com.ject.studytrip.trip.fixture.DailyGoalFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DailyGoalTestHelper {

    @Autowired private DailyGoalRepository dailyGoalRepository;

    public DailyGoal saveDailyGoal(Trip trip) {
        DailyGoal dailyGoal = DailyGoalFixture.createDailyGoal(trip);
        return dailyGoalRepository.save(dailyGoal);
    }

    public DailyGoal saveDeletedDailyGoal(Trip trip) {
        DailyGoal dailyGoal = DailyGoalFixture.createDailyGoal(trip);
        dailyGoal.updateDeletedAt();

        return dailyGoalRepository.save(dailyGoal);
    }
}
