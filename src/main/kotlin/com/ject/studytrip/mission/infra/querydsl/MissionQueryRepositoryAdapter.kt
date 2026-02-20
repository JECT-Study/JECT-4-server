package com.ject.studytrip.mission.infra.querydsl

import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.mission.domain.model.QMission.mission
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository
import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MissionQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : MissionQueryRepository {
    override fun findAllByIdsInFetchJoinStamp(ids: List<Long>): List<Mission> =
        queryFactory
            .selectFrom(mission)
            .join(mission.stamp, stamp)
            .fetchJoin()
            .where(mission.id.`in`(ids))
            .fetch()
}
