package com.ject.studytrip.mission.infra.querydsl;

import static com.ject.studytrip.mission.domain.model.QMission.mission;
import static com.ject.studytrip.stamp.domain.model.QStamp.stamp;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.mission.domain.repository.MissionCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionCommandRepositoryAdapter implements MissionCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByStampIdAndCompletedIsFalseAndDeletedAtIsNull(Long stampId) {
        Integer hit =
                queryFactory
                        .selectOne()
                        .from(mission)
                        .where(
                                mission.stamp.id.eq(stampId),
                                mission.completed.isFalse(),
                                mission.deletedAt.isNull())
                        .fetchFirst();

        return hit != null;
    }

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory.delete(mission).where(mission.deletedAt.isNotNull()).execute();
    }

    @Override
    public long deleteAllByDeletedStampOwner() {
        return queryFactory
                .delete(mission)
                .where(
                        mission.stamp.id.in(
                                JPAExpressions.select(stamp.id)
                                        .from(stamp)
                                        .where(stamp.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(mission.id)
                        .from(mission)
                        .join(mission.stamp, stamp)
                        .join(stamp.trip, trip)
                        .where(trip.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(mission).where(mission.id.in(ids)).execute();
    }
}
