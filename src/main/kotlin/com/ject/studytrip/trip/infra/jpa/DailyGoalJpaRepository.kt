package com.ject.studytrip.trip.infra.jpa

import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.data.jpa.repository.JpaRepository

interface DailyGoalJpaRepository : JpaRepository<DailyGoal, Long>
