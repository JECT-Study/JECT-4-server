package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.ject.studytrip.trip.domain.repository.DailyGoalCommandRepository
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class DailyGoalCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : DailyGoalCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(dailyGoal)
            .where(dailyGoal.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedTripOwner(): Long =
        queryFactory
            .delete(dailyGoal)
            .where(
                dailyGoal.trip.id.`in`(
                    JPAExpressions
                        .select(trip.id)
                        .from(trip)
                        .where(trip.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(dailyGoal.id)
                .from(dailyGoal)
                .join(dailyGoal.trip, trip)
                .where(trip.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(dailyGoal)
            .where(dailyGoal.id.`in`(ids))
            .execute()
    }
}
