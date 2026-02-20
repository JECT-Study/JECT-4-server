package com.ject.studytrip.trip.application.service

import com.ject.studytrip.trip.domain.factory.DailyGoalFactory
import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.repository.DailyGoalCommandRepository
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
import org.springframework.stereotype.Service

@Service
class DailyGoalCommandService(
    private val dailyGoalRepository: DailyGoalRepository,
    private val dailyGoalCommandRepository: DailyGoalCommandRepository,
) {
    fun createDailyGoal(
        trip: Trip,
        title: String,
    ): DailyGoal = dailyGoalRepository.save(DailyGoalFactory.create(trip, title))

    fun deleteDailyGoal(dailyGoal: DailyGoal) = dailyGoal.updateDeletedAt()

    fun hardDeleteDailyGoals() = dailyGoalCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteDailyGoalsOwnedByDeletedTrip() = dailyGoalCommandRepository.deleteAllByDeletedTripOwner()

    fun hardDeleteDailyGoalsOwnedByMember(memberId: Long) = dailyGoalCommandRepository.deleteAllByMemberId(memberId)
}
