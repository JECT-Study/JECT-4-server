package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog
import com.ject.studytrip.trip.domain.model.QTripReport.tripReport
import com.ject.studytrip.trip.domain.model.QTripReportStudyLog.tripReportStudyLog
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogCommandRepository
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripReportStudyLogCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : TripReportStudyLogCommandRepository {
    override fun deleteAllByDeletedMemberOwner(): Long =
        queryFactory
            .delete(tripReportStudyLog)
            .where(
                tripReportStudyLog.tripReport.id
                    .`in`(
                        JPAExpressions
                            .select(tripReport.id)
                            .from(tripReport)
                            .join(tripReport.member, member)
                            .where(member.deletedAt.isNotNull),
                    ).or(
                        tripReportStudyLog.studyLog.id.`in`(
                            JPAExpressions
                                .select(studyLog.id)
                                .from(studyLog)
                                .join(studyLog.member, member)
                                .where(member.deletedAt.isNotNull),
                        ),
                    ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(tripReportStudyLog.id)
                .from(tripReportStudyLog)
                .join(tripReportStudyLog.tripReport, tripReport)
                .where(tripReport.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(tripReportStudyLog)
            .where(tripReportStudyLog.id.`in`(ids))
            .execute()
    }
}
