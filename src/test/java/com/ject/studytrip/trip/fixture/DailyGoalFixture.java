package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.domain.factory.DailyGoalFactory;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.test.util.ReflectionTestUtils;

public class DailyGoalFixture {
    public static DailyGoal createDailyGoal(Trip trip) {
        return DailyGoalFactory.create(trip);
    }

    public static DailyGoal createDailyGoalWithId(Long id, Trip trip) {
        DailyGoal dailyGoal = DailyGoalFactory.create(trip);
        ReflectionTestUtils.setField(dailyGoal, "id", id);

        return dailyGoal;
    }

    public static DailyGoal createDeletedDailyGoal(Trip trip) {
        DailyGoal dailyGoal = DailyGoalFactory.create(trip);
        dailyGoal.updateDeletedAt();

        return dailyGoal;
    }
}
