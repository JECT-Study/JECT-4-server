package com.ject.studytrip.trip.infra.querydsl

import com.ject.studytrip.trip.domain.model.QTripReport.tripReport
import com.ject.studytrip.trip.domain.model.TripReport
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class TripReportQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : TripReportQueryRepository {
    override fun findAllActiveByMemberId(memberId: Long): List<TripReport> =
        queryFactory
            .selectFrom(tripReport)
            .where(
                tripReport.member.id.eq(memberId),
                tripReport.deletedAt.isNull,
            ).orderBy(tripReport.createdAt.desc())
            .fetch()

    override fun findImageUrlsByMemberId(memberId: Long): List<String> =
        queryFactory
            .select(tripReport.imageUrl)
            .from(tripReport)
            .where(tripReport.member.id.eq(memberId))
            .fetch()
}
