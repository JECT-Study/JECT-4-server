package com.ject.studytrip.trip.infra.querydsl;

import static com.ject.studytrip.member.domain.model.QMember.member;
import static com.ject.studytrip.trip.domain.model.QTripReport.tripReport;

import com.ject.studytrip.trip.domain.repository.TripReportCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportCommandRepositoryAdapter implements TripReportCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(tripReport).where(tripReport.deletedAt.isNotNull()).execute();
    }

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

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(tripReport.id)
                        .from(tripReport)
                        .where(tripReport.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(tripReport).where(tripReport.id.in(ids)).execute();
    }
}
