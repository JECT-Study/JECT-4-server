package com.ject.studytrip.pomodoro.infra.querydsl

import com.ject.studytrip.pomodoro.domain.model.QPomodoro.pomodoro
import com.ject.studytrip.pomodoro.domain.repository.PomodoroCommandRepository
import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PomodoroCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : PomodoroCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(pomodoro)
            .where(pomodoro.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedDailyGoalOwner(): Long =
        queryFactory
            .delete(pomodoro)
            .where(
                pomodoro.dailyGoal.id.`in`(
                    JPAExpressions
                        .select(dailyGoal.id)
                        .from(dailyGoal)
                        .where(dailyGoal.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids: List<Long> =
            queryFactory
                .select(pomodoro.id)
                .from(pomodoro)
                .join(pomodoro.dailyGoal, dailyGoal)
                .join(dailyGoal.trip, trip)
                .where(trip.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(pomodoro)
            .where(pomodoro.id.`in`(ids))
            .execute()
    }
}
