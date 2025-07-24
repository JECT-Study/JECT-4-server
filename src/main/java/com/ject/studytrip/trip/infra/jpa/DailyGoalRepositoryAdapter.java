package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.repository.DailyGoalRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyGoalRepositoryAdapter implements DailyGoalRepository {
    private final DailyGoalJpaRepository dailyGoalJpaRepository;

    @Override
    public DailyGoal save(DailyGoal dailyGoal) {
        return dailyGoalJpaRepository.save(dailyGoal);
    }

    @Override
    public Optional<DailyGoal> findById(Long id) {
        return dailyGoalJpaRepository.findById(id);
    }
}
