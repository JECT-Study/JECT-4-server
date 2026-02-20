package com.ject.studytrip.studylog.infra.querydsl

import com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository
import com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal
import com.ject.studytrip.trip.domain.model.QTripReportStudyLog.tripReportStudyLog
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class StudyLogQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : StudyLogQueryRepository {
    override fun findSliceByTripId(
        tripId: Long,
        pageable: Pageable,
        order: String,
    ): Slice<StudyLog> {
        val content =
            queryFactory
                .selectFrom(studyLog)
                .join(studyLog.dailyGoal, dailyGoal)
                .where(
                    dailyGoal.trip.id.eq(tripId),
                    dailyGoal.deletedAt.isNull,
                ).offset(pageable.offset)
                .limit(pageable.pageSize.toLong() + 1)
                .orderBy(*orderSpecifiers(order))
                .fetch()

        return toSlice(content, pageable)
    }

    override fun findSliceByTripReportIdOrderByCreatedAtDesc(
        tripReportId: Long,
        pageable: Pageable,
    ): Slice<StudyLog> {
        val content =
            queryFactory
                .select(studyLog)
                .from(tripReportStudyLog)
                .join(tripReportStudyLog.studyLog, studyLog)
                .where(
                    tripReportStudyLog.tripReport.id.eq(tripReportId),
                    studyLog.deletedAt.isNull,
                ).orderBy(studyLog.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong() + 1)
                .fetch()

        return toSlice(content, pageable)
    }

    override fun findAllIdsByTripIdOrderByCreatedDesc(tripId: Long): List<Long> =
        queryFactory
            .select(studyLog.id)
            .from(studyLog)
            .join(studyLog.dailyGoal, dailyGoal)
            .where(
                dailyGoal.trip.id.eq(tripId),
                studyLog.deletedAt.isNull,
                dailyGoal.deletedAt.isNull,
            ).orderBy(studyLog.createdAt.desc())
            .fetch()

    override fun findImageUrlsByMemberId(memberId: Long): List<String> =
        queryFactory
            .select(studyLog.imageUrl)
            .from(studyLog)
            .where(studyLog.member.id.eq(memberId))
            .fetch()

    override fun countStudyLogsByTripId(tripId: Long): Long =
        queryFactory
            .select(studyLog.count())
            .from(studyLog)
            .join(studyLog.dailyGoal, dailyGoal)
            .where(
                dailyGoal.trip.id.eq(tripId),
                studyLog.deletedAt.isNull,
                dailyGoal.deletedAt.isNull,
            ).fetchOne()
            ?: 0L

    override fun countActiveStudyLogsByMemberId(memberId: Long): Long =
        queryFactory
            .select(studyLog.count())
            .from(studyLog)
            .where(
                studyLog.member.id
                    .eq(memberId)
                    .and(studyLog.deletedAt.isNull),
            ).fetchOne()
            ?: 0L

    private fun orderSpecifiers(order: String): Array<OrderSpecifier<*>> =
        if (order.equals("OLDEST", ignoreCase = true)) {
            arrayOf(studyLog.createdAt.asc(), studyLog.id.asc())
        } else {
            arrayOf(studyLog.createdAt.desc(), studyLog.id.desc())
        }

    private fun <T> toSlice(
        content: List<T>,
        pageable: Pageable,
    ): Slice<T> {
        val hasNext = content.size > pageable.pageSize
        val result = content.take(pageable.pageSize)
        return SliceImpl(result, pageable, hasNext)
    }
}
