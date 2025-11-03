package com.ject.studytrip.studylog.infra.querydsl;

import com.ject.studytrip.member.domain.model.QMember;
import com.ject.studytrip.studylog.domain.model.QStudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.repository.StudyLogQueryRepository;
import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.ject.studytrip.trip.domain.model.QTripReportStudyLog;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogQueryRepositoryAdapter implements StudyLogQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QStudyLog studyLog = QStudyLog.studyLog;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;
    private final QMember member = QMember.member;
    private final QTripReportStudyLog tripReportStudyLog = QTripReportStudyLog.tripReportStudyLog;

    @Override
    public long countActiveStudyLogsByMemberId(Long memberId) {
        Long count =
                queryFactory
                        .select(studyLog.count())
                        .from(studyLog)
                        .where(studyLog.member.id.eq(memberId).and(studyLog.deletedAt.isNull()))
                        .fetchOne();

        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public Slice<StudyLog> findSliceByTripId(Long tripId, Pageable pageable, String order) {
        List<StudyLog> content =
                queryFactory
                        .selectFrom(studyLog)
                        .join(studyLog.dailyGoal, dailyGoal)
                        .where(dailyGoal.trip.id.eq(tripId), dailyGoal.deletedAt.isNull())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .orderBy(orderSpecifiers(order))
                        .fetch();

        List<StudyLog> result = content;
        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            result = content.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(studyLog).where(studyLog.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedMemberOwner() {
        return queryFactory
                .delete(studyLog)
                .where(
                        studyLog.member.id.in(
                                JPAExpressions.select(member.id)
                                        .from(member)
                                        .where(member.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByDeletedDailyGoalOwner() {
        return queryFactory
                .delete(studyLog)
                .where(
                        studyLog.dailyGoal.id.in(
                                JPAExpressions.select(dailyGoal.id)
                                        .from(dailyGoal)
                                        .where(dailyGoal.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long countStudyLogsByTripId(Long tripId) {
        Long count =
                queryFactory
                        .select(studyLog.count())
                        .from(studyLog)
                        .join(studyLog.dailyGoal, dailyGoal)
                        .where(
                                dailyGoal.trip.id.eq(tripId),
                                studyLog.deletedAt.isNull(),
                                dailyGoal.deletedAt.isNull())
                        .fetchOne();

        return count == null ? 0L : count;
    }

    @Override
    public Slice<StudyLog> findSliceByTripReportIdOrderByCreatedAtDesc(
            Long tripReportId, Pageable pageable) {
        List<StudyLog> content =
                queryFactory
                        .select(studyLog)
                        .from(tripReportStudyLog)
                        .join(tripReportStudyLog.studyLog, studyLog)
                        .where(
                                tripReportStudyLog.tripReport.id.eq(tripReportId),
                                studyLog.deletedAt.isNull())
                        .orderBy(studyLog.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        List<StudyLog> result = hasNext ? content.subList(0, pageable.getPageSize()) : content;

        return new SliceImpl<>(result, pageable, hasNext);
    }

    @Override
    public List<Long> findAllIdsByTripIdOrderByCreatedDesc(Long tripId) {
        return queryFactory
                .select(studyLog.id)
                .from(studyLog)
                .join(studyLog.dailyGoal, dailyGoal)
                .where(
                        dailyGoal.trip.id.eq(tripId),
                        studyLog.deletedAt.isNull(),
                        dailyGoal.deletedAt.isNull())
                .orderBy(studyLog.createdAt.desc())
                .fetch();
    }

    @Override
    public List<String> findImageUrlsByMemberId(Long memberId) {
        return queryFactory
                .select(studyLog.imageUrl)
                .from(studyLog)
                .where(studyLog.member.id.eq(memberId))
                .fetch();
    }

    @Override
    public long deleteByMemberId(Long memberId) {
        return queryFactory.delete(studyLog).where(studyLog.member.id.eq(memberId)).execute();
    }

    private OrderSpecifier<?>[] orderSpecifiers(String order) {
        return (order.equalsIgnoreCase("OLDEST"))
                ? new OrderSpecifier<?>[] {studyLog.createdAt.asc(), studyLog.id.asc()}
                : new OrderSpecifier<?>[] {studyLog.createdAt.desc(), studyLog.id.desc()};
    }
}
