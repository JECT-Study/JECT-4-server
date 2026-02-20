package com.ject.studytrip.studylog.infra.querydsl

import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogCommandRepository
import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StudyLogCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StudyLogCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(studyLog)
            .where(studyLog.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedMemberOwner(): Long =
        queryFactory
            .delete(studyLog)
            .where(
                studyLog.member.id.`in`(
                    JPAExpressions
                        .select(member.id)
                        .from(member)
                        .where(member.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByDeletedDailyGoalOwner(): Long =
        queryFactory
            .delete(studyLog)
            .where(
                studyLog.dailyGoal.id.`in`(
                    JPAExpressions
                        .select(dailyGoal.id)
                        .from(dailyGoal)
                        .where(dailyGoal.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteByMemberId(memberId: Long): Long =
        queryFactory
            .delete(studyLog)
            .where(studyLog.member.id.eq(memberId))
            .execute()
}
