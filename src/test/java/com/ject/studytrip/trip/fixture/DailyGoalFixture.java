package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.domain.factory.DailyGoalFactory;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.test.util.ReflectionTestUtils;

public class DailyGoalFixture {
    private static final String DEFAULT_TITLE = "TEST TITLE";

    public static DailyGoal createDailyGoal(Trip trip) {
        return DailyGoalFactory.create(trip, DEFAULT_TITLE);
    }

    public static DailyGoal createDailyGoalWithId(Long id, Trip trip) {
        DailyGoal dailyGoal = DailyGoalFactory.create(trip, DEFAULT_TITLE);
        ReflectionTestUtils.setField(dailyGoal, "id", id);

        return dailyGoal;
    }
}
