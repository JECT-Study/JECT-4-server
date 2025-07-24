package com.ject.studytrip.mission.infra.jpa;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.model.QMission;
import com.ject.studytrip.mission.domain.repository.MissionQueryRepository;
import com.ject.studytrip.stamp.domain.model.QStamp;
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

    @Override
    public List<Mission> findAllByIdsInFetchJoinStamp(List<Long> ids) {
        return queryFactory
                .selectFrom(mission)
                .join(mission.stamp, stamp)
                .fetchJoin()
                .where(mission.id.in(ids))
                .fetch();
    }
}
