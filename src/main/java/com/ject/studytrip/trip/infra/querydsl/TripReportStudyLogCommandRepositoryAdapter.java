package com.ject.studytrip.trip.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;
import static com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog;
import static com.ject.studytrip.trip.domain.model.QTripReport.tripReport;
import static com.ject.studytrip.trip.domain.model.QTripReportStudyLog.tripReportStudyLog;

import com.ject.studytrip.trip.domain.repository.TripReportStudyLogCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportStudyLogCommandRepositoryAdapter
        implements TripReportStudyLogCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedMemberOwner() {
        return queryFactory
                .delete(tripReportStudyLog)
                .where(
                        tripReportStudyLog
                                .tripReport
                                .id
                                .in(
                                        JPAExpressions.select(tripReport.id)
                                                .from(tripReport)
                                                .join(tripReport.member, member)
                                                .where(member.deletedAt.isNotNull()))
                                .or(
                                        tripReportStudyLog.studyLog.id.in(
                                                JPAExpressions.select(studyLog.id)
                                                        .from(studyLog)
                                                        .join(studyLog.member, member)
                                                        .where(member.deletedAt.isNotNull()))))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(tripReportStudyLog.id)
                        .from(tripReportStudyLog)
                        .join(tripReportStudyLog.tripReport, tripReport)
                        .where(tripReport.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory
                .delete(tripReportStudyLog)
                .where(tripReportStudyLog.id.in(ids))
                .execute();
    }
}
