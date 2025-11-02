package com.ject.studytrip.mission.infra.querydsl;

import static com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission;
import static com.ject.studytrip.mission.domain.model.QMission.mission;
import static com.ject.studytrip.stamp.domain.model.QStamp.stamp;
import static com.ject.studytrip.trip.domain.model.QDailyGoal.dailyGoal;
import static com.ject.studytrip.trip.domain.model.QTrip.trip;

import com.ject.studytrip.mission.domain.repository.DailyMissionCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyMissionCommandRepositoryAdapter implements DailyMissionCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory
                .delete(dailyMission)
                .where(dailyMission.deletedAt.isNotNull())
                .execute();
    }

    @Override
    public long deleteAllByDeletedMissionOwner() {
        return queryFactory
                .delete(dailyMission)
                .where(
                        dailyMission.mission.id.in(
                                JPAExpressions.select(mission.id)
                                        .from(mission)
                                        .where(mission.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByDeletedDailyGoalOwner() {
        return queryFactory
                .delete(dailyMission)
                .where(
                        dailyMission.dailyGoal.id.in(
                                JPAExpressions.select(dailyGoal.id)
                                        .from(dailyGoal)
                                        .where(dailyGoal.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(dailyMission.id)
                        .from(dailyMission)
                        .join(dailyMission.mission, mission)
                        .join(mission.stamp, stamp)
                        .join(stamp.trip, trip)
                        .where(trip.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory.delete(dailyMission).where(dailyMission.id.in(ids)).execute();
    }
}
