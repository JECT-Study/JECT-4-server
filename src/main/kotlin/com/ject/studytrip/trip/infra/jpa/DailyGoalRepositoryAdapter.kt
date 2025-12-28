package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.DailyGoal
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class DailyGoalRepositoryAdapter(
    private val dailyGoalJpaRepository: DailyGoalJpaRepository,
) : DailyGoalRepository {
    override fun save(dailyGoal: DailyGoal): DailyGoal = dailyGoalJpaRepository.save(dailyGoal)

    override fun findById(dailyGoalId: Long): Optional<DailyGoal> = dailyGoalJpaRepository.findById(dailyGoalId)
}
