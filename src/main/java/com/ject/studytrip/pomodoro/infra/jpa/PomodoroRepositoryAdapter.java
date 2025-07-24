package com.ject.studytrip.pomodoro.infra.jpa;

import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.pomodoro.domain.repository.PomodoroRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PomodoroRepositoryAdapter implements PomodoroRepository {
    private final PomodoroJpaRepository pomodoroJpaRepository;

    @Override
    public Pomodoro save(Pomodoro pomodoro) {
        return pomodoroJpaRepository.save(pomodoro);
    }

    @Override
    public Optional<Pomodoro> findByDailyGoalId(Long dailyGoalId) {
        return pomodoroJpaRepository.findByDailyGoalId(dailyGoalId);
    }
}
