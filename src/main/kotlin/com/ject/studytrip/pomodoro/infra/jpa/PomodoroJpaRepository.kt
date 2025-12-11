package com.ject.studytrip.pomodoro.infra.jpa

import com.ject.studytrip.pomodoro.domain.model.Pomodoro
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PomodoroJpaRepository : JpaRepository<Pomodoro, Long> {
    fun findByDailyGoalId(dailyGoalId: Long): Optional<Pomodoro>
}
