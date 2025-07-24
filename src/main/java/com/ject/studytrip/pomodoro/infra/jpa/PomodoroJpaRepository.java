package com.ject.studytrip.pomodoro.infra.jpa;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroJpaRepository extends JpaRepository<Pomodoro, Long> {

    Optional<Pomodoro> findByDailyGoalId(Long dailyGoalId);
}
