package com.ject.studytrip.mission.infra.querydsl

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission
import com.ject.studytrip.mission.domain.model.QMission.mission
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository
import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class DailyMissionQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : DailyMissionQueryRepository {
    override fun findAllByDailyGoalIdFetchJoinMission(dailyGoalId: Long): List<DailyMission> =
        queryFactory
            .selectFrom(dailyMission)
            .join(dailyMission.mission, mission)
            .fetchJoin()
            .where(
                dailyMission.dailyGoal.id.eq(dailyGoalId),
                dailyMission.deletedAt.isNull,
            ).fetch()

    override fun findAllWithMissionAndStampByIds(ids: List<Long>): List<DailyMission> =
        queryFactory
            .selectFrom(dailyMission)
            .join(dailyMission.mission, mission)
            .fetchJoin()
            .join(mission.stamp, stamp)
            .fetchJoin()
            .where(dailyMission.id.`in`(ids))
            .fetch()
}
