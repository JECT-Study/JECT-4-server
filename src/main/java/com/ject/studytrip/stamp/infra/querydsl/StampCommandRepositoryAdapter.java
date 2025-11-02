package com.ject.studytrip.stamp.infra.querydsl;

import static com.ject.studytrip.stamp.domain.model.QStamp.stamp;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.stamp.domain.repository.StampCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampCommandRepositoryAdapter implements StampCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(Long tripId) {
        Integer hit =
                queryFactory
                        .selectOne()
                        .from(stamp)
                        .where(
                                stamp.trip.id.eq(tripId),
                                stamp.completed.isFalse(),
                                stamp.deletedAt.isNull())
                        .fetchOne();

        return hit != null;
    }

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(stamp).where(stamp.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedTripOwner() {
        return queryFactory
                .delete(stamp)
                .where(
                        stamp.trip.id.in(
                                JPAExpressions.select(trip.id)
                                        .from(trip)
                                        .where(trip.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(stamp.id)
                        .from(stamp)
                        .join(stamp.trip, trip)
                        .where(trip.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(stamp).where(stamp.id.in(ids)).execute();
    }
}
