package com.ject.studytrip.stamp.infra.querydsl

import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.ject.studytrip.stamp.domain.repository.StampCommandRepository
import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class StampCommandRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StampCommandRepository {
    override fun existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId: Long): Boolean {
        val hit =
            queryFactory
                .selectOne()
                .from(stamp)
                .where(
                    stamp.trip.id.eq(tripId),
                    stamp.completed.isFalse,
                    stamp.deletedAt.isNull,
                ).fetchOne()

        return hit != null
    }

    override fun deleteAllByDeletedAtIsNotNull(): Long =
        queryFactory
            .delete(stamp)
            .where(stamp.deletedAt.isNotNull)
            .execute()

    override fun deleteAllByDeletedTripOwner(): Long =
        queryFactory
            .delete(stamp)
            .where(
                stamp.trip.id.`in`(
                    JPAExpressions
                        .select(trip.id)
                        .from(trip)
                        .where(trip.deletedAt.isNotNull),
                ),
            ).execute()

    override fun deleteAllByMemberId(memberId: Long): Long {
        val ids =
            queryFactory
                .select(stamp.id)
                .from(stamp)
                .join(stamp.trip, trip)
                .where(trip.member.id.eq(memberId))
                .fetch()

        if (ids.isEmpty()) return 0L

        return queryFactory
            .delete(stamp)
            .where(stamp.id.`in`(ids))
            .execute()
    }
}
