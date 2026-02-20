package com.ject.studytrip.trip.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal

object DailyGoalPolicy {
    fun validateNotDeleted(dailyGoal: DailyGoal) {
        if (dailyGoal.isDeleted()) {
            throw CustomException(DailyGoalErrorCode.DAILY_GOAL_ALREADY_DELETED)
        }
    }

    fun validateDailyGoalBelongsToTrip(
        dailyGoal: DailyGoal,
        tripId: Long,
    ) {
        if (dailyGoal.trip.id != tripId) {
            throw CustomException(DailyGoalErrorCode.DAILY_GOAL_NOT_BELONGS_TO_TRIP)
        }
    }
}
