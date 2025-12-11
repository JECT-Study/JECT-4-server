package com.ject.studytrip.pomodoro.infra.jpa

import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class PomodoroRepositoryAdapter(
    private val pomodoroJpaRepository: PomodoroJpaRepository,
) : PomodoroRepository {
    override fun save(pomodoro: Pomodoro): Pomodoro = pomodoroJpaRepository.save(pomodoro)

    override fun findByDailyGoalId(dailyGoalId: Long): Optional<Pomodoro> = pomodoroJpaRepository.findByDailyGoalId(dailyGoalId)
}
