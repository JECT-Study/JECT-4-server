package com.ject.studytrip.pomodoro.infra.querydsl

import com.ject.studytrip.pomodoro.domain.model.QPomodoro.pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroQueryRepository
import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PomodoroQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : PomodoroQueryRepository {
    override fun sumFocusHoursByTripId(tripId: Long): Long =
        queryFactory
            .select(pomodoro.totalFocusTimeInSeconds.sum())
            .from(pomodoro)
            .join(pomodoro.dailyGoal, dailyGoal)
            .join(dailyGoal.trip, trip)
            .where(
                trip.id.eq(tripId),
                pomodoro.deletedAt.isNull,
                dailyGoal.deletedAt.isNull,
                trip.deletedAt.isNull,
            ).fetchOne()
            ?.toLong()
            ?.div(3600)
            ?: 0L
}
