package com.ject.studytrip.studylog.infra.querydsl

import com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission
import com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog
import com.ject.studytrip.studylog.domain.model.QStudyLogDailyMission.studyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionCommandRepository
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StudyLogDailyMissionCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StudyLogDailyMissionCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(studyLogDailyMission)
            .where(studyLogDailyMission.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedDailyMissionOwner(): Long =
        queryFactory
            .delete(studyLogDailyMission)
            .where(
                studyLogDailyMission.dailyMission.id.`in`(
                    JPAExpressions
                        .select(dailyMission.id)
                        .from(dailyMission)
                        .where(dailyMission.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByDeletedStudyLogOwner(): Long =
        queryFactory
            .delete(studyLogDailyMission)
            .where(
                studyLogDailyMission.studyLog.id.`in`(
                    JPAExpressions
                        .select(studyLog.id)
                        .from(studyLog)
                        .where(studyLog.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(studyLogDailyMission.id)
                .from(studyLogDailyMission)
                .join(studyLogDailyMission.studyLog, studyLog)
                .where(studyLog.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(studyLogDailyMission)
            .where(studyLogDailyMission.id.`in`(ids))
            .execute()
    }
}
