package com.ject.studytrip.stamp.infra.querydsl

import com.ject.studytrip.stamp.domain.model.QStamp.stamp
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class StampQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StampQueryRepository {
    override fun findStampsToShiftAfterOrder(
        tripId: Long,
        deletedOrder: Int,
    ): List<Stamp> =
        queryFactory
            .selectFrom(stamp)
            .where(
                stamp.trip.id.eq(tripId),
                stamp.stampOrder.gt(deletedOrder),
                stamp.deletedAt.isNull,
            ).orderBy(stamp.stampOrder.asc())
            .fetch()

    // 완료되지 않은 스탬프 중 가장 첫번째 스탬프 조회
    override fun findFirstIncompleteStampByTripId(tripId: Long): Optional<Stamp> =
        Optional.ofNullable(
            queryFactory
                .selectFrom(stamp)
                .where(
                    stamp.trip.id.eq(tripId),
                    stamp.completed.isFalse,
                    stamp.deletedAt.isNull,
                ).orderBy(stamp.stampOrder.asc())
                .fetchFirst(),
        )

    override fun findNextStampOrderByTripId(tripId: Long): Int =
        queryFactory
            .select(
                stamp.stampOrder
                    .max()
                    .coalesce(0)
                    .add(1),
            ).from(stamp)
            .where(
                stamp.trip.id.eq(tripId),
                stamp.deletedAt.isNull,
            ).fetchOne() ?: 1
}
