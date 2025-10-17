package com.ject.studytrip.trip.infra.querydsl;

import com.ject.studytrip.member.domain.model.QMember;
import com.ject.studytrip.trip.domain.model.QTripReport;
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportQueryRepositoryAdapter implements TripReportQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QTripReport tripReport = QTripReport.tripReport;
    private final QMember member = QMember.member;

    @Override
    public long deleteAllByDeletedMemberOwner() {
        return queryFactory
                .delete(tripReport)
                .where(
                        tripReport.member.id.in(
                                JPAExpressions.select(member.id)
                                        .from(member)
                                        .where(member.deletedAt.isNotNull())))
                .execute();
    }
}
