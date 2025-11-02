package com.ject.studytrip.stamp.infra.querydsl;

import static com.ject.studytrip.stamp.domain.model.QStamp.stamp;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampQueryRepositoryAdapter implements StampQueryRepository {
    private final JPAQueryFactory queryFactory;

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
    public int findNextStampOrderByTripId(Long tripId) {
        Integer lastOrder =
                queryFactory
                        .select(stamp.stampOrder.max().coalesce(0))
                        .from(stamp)
                        .where(stamp.trip.id.eq(tripId), stamp.deletedAt.isNull())
                        .fetchOne();

        return lastOrder != null ? lastOrder + 1 : 1;
    }
}
