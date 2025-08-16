package com.ject.studytrip.mission.infra.querydsl;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.QDailyMission;
import com.ject.studytrip.mission.domain.model.QMission;
import com.ject.studytrip.mission.domain.repository.DailyMissionQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyMissionQueryRepositoryAdapter implements DailyMissionQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QDailyMission dailyMission = QDailyMission.dailyMission;
    private final QMission mission = QMission.mission;

    @Override
    public List<DailyMission> findAllByDailyGoalIdFetchJoinMission(Long dailyGoalId) {
        return queryFactory
                .selectFrom(dailyMission)
                .join(dailyMission.mission, mission)
                .fetchJoin()
                .where(dailyMission.dailyGoal.id.eq(dailyGoalId), dailyMission.deletedAt.isNull())
                .fetch();
    }
}
