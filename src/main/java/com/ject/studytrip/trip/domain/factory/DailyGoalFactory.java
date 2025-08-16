package com.ject.studytrip.trip.domain.factory;

import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyGoalFactory {
    public static DailyGoal create(Trip trip, String title) {
        return DailyGoal.of(trip, title);
    }
}
