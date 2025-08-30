package com.ject.studytrip.stamp.infra.querydsl;

import com.ject.studytrip.stamp.domain.model.QStamp;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.ject.studytrip.trip.domain.model.QTrip;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampQueryRepositoryAdapter implements StampQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QStamp stamp = QStamp.stamp;
    private final QTrip trip = QTrip.trip;

    @Override
    public List<Stamp> findStampsToShiftAfterOrder(Long tripId, int deletedOrder) {
        return queryFactory
                .selectFrom(stamp)
                .where(
                        stamp.trip.id.eq(tripId),
                        stamp.stampOrder.gt(deletedOrder),
                        stamp.deletedAt.isNull())
                .orderBy(stamp.stampOrder.asc())
                .fetch();
    }

    // 완료되지 않은 스탬프 중 가장 첫번째 스탬프 조회
    @Override
    public Optional<Stamp> findFirstIncompleteStampByTripId(Long tripId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(stamp)
                        .where(
                                stamp.trip.id.eq(tripId),
                                stamp.completed.isFalse(),
                                stamp.deletedAt.isNull())
                        .orderBy(stamp.stampOrder.asc())
                        .fetchFirst());
    }

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
}
