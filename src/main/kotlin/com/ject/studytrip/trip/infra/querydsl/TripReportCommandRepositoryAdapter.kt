package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.trip.domain.model.QTripReport.tripReport
import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripReportCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : TripReportCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(tripReport)
            .where(tripReport.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedMemberOwner(): Long =
        queryFactory
            .delete(tripReport)
            .where(
                tripReport.member.id.`in`(
                    JPAExpressions
                        .select(member.id)
                        .from(member)
                        .where(member.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(tripReport.id)
                .from(tripReport)
                .where(tripReport.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(tripReport)
            .where(tripReport.id.`in`(ids))
            .execute()
    }
}
