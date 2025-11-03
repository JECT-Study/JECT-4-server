package com.ject.studytrip.trip.infra.querydsl;

import com.ject.studytrip.member.domain.model.QMember;
import com.ject.studytrip.studylog.domain.model.QStudyLog;
import com.ject.studytrip.trip.domain.model.QTripReport;
import com.ject.studytrip.trip.domain.model.QTripReportStudyLog;
import com.ject.studytrip.trip.domain.repository.TripReportStudyLogQueryRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportStudyLogQueryRepositoryAdapter implements TripReportStudyLogQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QTripReportStudyLog tripReportStudyLog = QTripReportStudyLog.tripReportStudyLog;
    private final QTripReport tripReport = QTripReport.tripReport;
    private final QStudyLog studyLog = QStudyLog.studyLog;
    private final QMember member = QMember.member;

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
