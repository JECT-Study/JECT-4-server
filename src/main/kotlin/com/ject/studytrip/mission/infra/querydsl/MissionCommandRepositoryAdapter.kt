package com.ject.studytrip.mission.infra.querydsl

import com.ject.studytrip.mission.domain.model.QMission.mission
import com.ject.studytrip.mission.domain.repository.MissionCommandRepository
import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MissionCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : MissionCommandRepository {
    override fun existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(stampId: Long): Boolean {
        val hit =
            queryFactory
                .selectOne()
                .from(mission)
                .where(
                    mission.stamp.id.eq(stampId),
                    mission.completed.isFalse,
                    mission.deletedAt.isNull,
                ).fetchFirst()

        return hit != null
    }

    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(mission)
            .where(mission.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedStampOwner(): Long =
        queryFactory
            .delete(mission)
            .where(
                mission.stamp.id.`in`(
                    JPAExpressions
                        .select(stamp.id)
                        .from(stamp)
                        .where(stamp.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(mission.id)
                .from(mission)
                .join(mission.stamp, stamp)
                .join(stamp.trip, trip)
                .where(trip.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(mission)
            .where(mission.id.`in`(ids))
            .execute()
    }
}
