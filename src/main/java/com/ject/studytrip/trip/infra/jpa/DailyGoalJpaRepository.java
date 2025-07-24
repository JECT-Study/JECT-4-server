package com.ject.studytrip.trip.infra.jpa;

import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyGoalJpaRepository extends JpaRepository<DailyGoal, Long> {}
