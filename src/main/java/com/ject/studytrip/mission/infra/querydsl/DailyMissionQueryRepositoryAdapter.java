package com.ject.studytrip.mission.infra.querydsl;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.QDailyMission;
import com.ject.studytrip.mission.domain.model.QMission;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.ject.studytrip.stamp.domain.model.QStamp;
import com.ject.studytrip.trip.domain.model.QDailyGoal;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyMissionQueryRepositoryAdapter implements DailyMissionQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QDailyMission dailyMission = QDailyMission.dailyMission;
    private final QStamp stamp = QStamp.stamp;
    private final QMission mission = QMission.mission;
    private final QDailyGoal dailyGoal = QDailyGoal.dailyGoal;

    @Override
    public List<DailyMission> findAllByDailyGoalIdFetchJoinMission(Long dailyGoalId) {
        return queryFactory
                .selectFrom(dailyMission)
                .join(dailyMission.mission, mission)
                .fetchJoin()
                .where(dailyMission.dailyGoal.id.eq(dailyGoalId), dailyMission.deletedAt.isNull())
                .fetch();
    }

    @Override
    public List<DailyMission> findAllWithMissionAndStampByIds(List<Long> ids) {
        return queryFactory
                .selectFrom(dailyMission)
                .join(dailyMission.mission, mission)
                .fetchJoin()
                .join(mission.stamp, stamp)
                .fetchJoin()
                .where(dailyMission.id.in(ids))
                .fetch();
    }

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
}
