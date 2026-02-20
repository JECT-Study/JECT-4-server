package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.member.domain.model.QMember.member
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.ject.studytrip.trip.domain.repository.TripCommandRepository
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : TripCommandRepository {
    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(trip)
            .where(trip.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedMemberOwner(): Long =
        queryFactory
            .delete(trip)
            .where(
                trip.member.id.`in`(
                    JPAExpressions
                        .select(member.id)
                        .from(member)
                        .where(member.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long =
        queryFactory
            .delete(trip)
            .where(trip.member.id.eq(memberId))
            .execute()
}
