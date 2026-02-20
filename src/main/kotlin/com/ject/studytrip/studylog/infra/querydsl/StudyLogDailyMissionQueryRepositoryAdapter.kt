package com.ject.studytrip.studylog.infra.querydsl

import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission
import com.ject.studytrip.mission.domain.model.QMission.mission
import com.ject.studytrip.studylog.domain.model.QStudyLogDailyMission.studyLogDailyMission
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StudyLogDailyMissionQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StudyLogDailyMissionQueryRepository {
    override fun findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds: List<Long>): Map<Long, List<StudyLogDailyMission>> =
        queryFactory
            .selectFrom(studyLogDailyMission)
            .join(studyLogDailyMission.dailyMission, dailyMission)
            .fetchJoin()
            .join(dailyMission.mission, mission)
            .fetchJoin()
            .where(studyLogDailyMission.studyLog.id.`in`(studyLogIds))
            .fetch()
            .groupBy { it.studyLog.id.requireId() }
}
