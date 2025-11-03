package com.ject.studytrip.mission.infra.querydsl;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.model.QMission;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.stamp.domain.model.QStamp;
import com.ject.studytrip.trip.domain.model.QTrip;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionQueryRepositoryAdapter implements MissionQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QMission mission = QMission.mission;
    private final QStamp stamp = QStamp.stamp;
    private final QTrip trip = QTrip.trip;

    @Override
    public List<Mission> findAllByIdsInFetchJoinStamp(List<Long> ids) {
        return queryFactory
                .selectFrom(mission)
                .join(mission.stamp, stamp)
                .fetchJoin()
                .where(mission.id.in(ids))
                .fetch();
    }

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
