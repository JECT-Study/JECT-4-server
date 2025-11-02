package com.ject.studytrip.mission.infra.querydsl;

import static com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission;
import static com.ject.studytrip.mission.domain.model.QMission.mission;
import static com.ject.studytrip.stamp.domain.model.QStamp.stamp;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyMissionQueryRepositoryAdapter implements DailyMissionQueryRepository {
    private final JPAQueryFactory queryFactory;

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
}
