package com.ject.studytrip.trip.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.DailyGoalErrorCode
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.policy.DailyGoalPolicy
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
import org.springframework.stereotype.Service

@Service
class DailyGoalQueryService(
    private val dailyGoalRepository: DailyGoalRepository,
) {
    fun getValidDailyGoal(
        tripId: Long,
        dailyGoalId: Long,
    ): DailyGoal {
        val dailyGoal =
            dailyGoalRepository
                .findById(dailyGoalId)
                .orElseThrow { CustomException(DailyGoalErrorCode.DAILY_GOAL_NOT_FOUND) }

        DailyGoalPolicy.validateDailyGoalBelongsToTrip(dailyGoal, tripId)
        DailyGoalPolicy.validateNotDeleted(dailyGoal)

        return dailyGoal
    }
}
