package com.ject.studytrip.trip.infra.querydsl;

import static com.ject.studytrip.trip.domain.model.QTripReport.tripReport;

import com.ject.studytrip.trip.domain.model.TripReport;
import com.ject.studytrip.trip.domain.repository.TripReportQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TripReportQueryRepositoryAdapter implements TripReportQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<TripReport> findAllActiveByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(tripReport)
                .where(tripReport.member.id.eq(memberId), tripReport.deletedAt.isNull())
                .orderBy(tripReport.createdAt.desc())
                .fetch();
    }

    @Override
    public List<String> findImageUrlsByMemberId(Long memberId) {
        return queryFactory
                .select(tripReport.imageUrl)
                .from(tripReport)
                .where(tripReport.member.id.eq(memberId))
                .fetch();
    }
}
