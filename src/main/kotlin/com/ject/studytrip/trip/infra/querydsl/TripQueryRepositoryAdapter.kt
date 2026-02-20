package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.trip.domain.model.QTrip.trip
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.domain.repository.TripQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class TripQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : TripQueryRepository {
    override fun findSliceByMemberIdAndCompletedFalseAndDeletedAtIsNull(
        memberId: Long,
        pageable: Pageable,
    ): Slice<Trip> {
        val content =
            queryFactory
                .selectFrom(trip)
                .where(
                    trip.member.id.eq(memberId),
                    trip.completed.isFalse,
                    trip.deletedAt.isNull,
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong() + 1)
                .fetch()

        val hasNext = content.size > pageable.pageSize
        val result = content.take(pageable.pageSize)

        return SliceImpl(result, pageable, hasNext)
    }

    override fun countActiveTripsByMemberIdAndCategory(
        memberId: Long,
        category: TripCategory,
    ): Long =
        queryFactory
            .select(trip.count())
            .from(trip)
            .where(
                trip.member.id.eq(memberId),
                trip.deletedAt.isNull,
                trip.category.eq(category),
            ).fetchOne()
            ?: 0L
}
