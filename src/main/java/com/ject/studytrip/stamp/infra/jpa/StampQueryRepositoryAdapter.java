package com.ject.studytrip.stamp.infra.jpa;

import com.ject.studytrip.stamp.domain.model.QStamp;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StampQueryRepositoryAdapter implements StampQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QStamp stamp = QStamp.stamp;

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
}
