package com.ject.studytrip.mission.infra.querydsl;

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

    //    @Override
    //    public long countByStampIdAndDeletedAtIsNull(Long stampId) {
    //        Long count =
    //                queryFactory
    //                        .select(mission.count())
    //                        .from(mission)
    //                        .where(mission.stamp.id.eq(stampId), mission.deletedAt.isNull())
    //                        .fetchOne();
    //
    //        return Optional.ofNullable(count).orElse(0L);
    //    }
    //
    //    @Override
    //    public long countByStampIdAndCompletedIsTrueAndDeletedAtIsNull(Long stampId) {
    //        Long count =
    //                queryFactory
    //                        .select(mission.count())
    //                        .from(mission)
    //                        .where(
    //                                mission.stamp.id.eq(stampId),
    //                                mission.completed.isTrue(),
    //                                mission.deletedAt.isNull())
    //                        .fetchOne();
    //
    //        return Optional.ofNullable(count).orElse(0L);
    //    }
}
