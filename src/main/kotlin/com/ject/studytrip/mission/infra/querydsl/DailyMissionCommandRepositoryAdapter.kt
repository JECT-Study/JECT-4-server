package com.ject.studytrip.mission.infra.querydsl

import com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission
import com.ject.studytrip.mission.domain.model.QMission.mission
import com.ject.studytrip.mission.domain.repository.DailyMissionCommandRepository
import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class DailyMissionCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : DailyMissionCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(dailyMission)
            .where(dailyMission.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedMissionOwner(): Long =
        queryFactory
            .delete(dailyMission)
            .where(
                dailyMission.mission.id.`in`(
                    JPAExpressions
                        .select(mission.id)
                        .from(mission)
                        .where(mission.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByDeletedDailyGoalOwner(): Long =
        queryFactory
            .delete(dailyMission)
            .where(
                dailyMission.dailyGoal.id.`in`(
                    JPAExpressions
                        .select(dailyGoal.id)
                        .from(dailyGoal)
                        .where(dailyGoal.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(dailyMission.id)
                .from(dailyMission)
                .join(dailyMission.mission, mission)
                .join(mission.stamp, stamp)
                .join(stamp.trip, trip)
                .where(trip.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(dailyMission)
            .where(dailyMission.id.`in`(ids))
            .execute()
    }
}
