package com.ject.studytrip.trip.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyGoalPolicy {
    public static void validateBelongsToTrip(DailyGoal dailyGoal, Long tripId) {
        if (!dailyGoal.getTrip().getId().equals(tripId))
            throw new CustomException(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONG_TO_TRIP);
    }

    public static void validateNotDeleted(DailyGoal dailyGoal) {
        if (dailyGoal.getDeletedAt() != null)
            throw new CustomException(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED);
    }
}
