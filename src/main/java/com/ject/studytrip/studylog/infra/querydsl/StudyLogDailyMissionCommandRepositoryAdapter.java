package com.ject.studytrip.studylog.infra.querydsl;

import static com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission;
import static com.ject.studytrip.studylog.domain.model.QStudyLog.studyLog;
import static com.ject.studytrip.studylog.domain.model.QStudyLogDailyMission.studyLogDailyMission;

import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionCommandRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogDailyMissionCommandRepositoryAdapter
        implements StudyLogDailyMissionCommandRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAllByDeletedAtIsNotNull() {
        return queryFactory
                .delete(studyLogDailyMission)
                .where(studyLogDailyMission.deletedAt.isNotNull())
                .execute();
    }

    @Override
    public long deleteAllByDeletedDailyMissionOwner() {
        return queryFactory
                .delete(studyLogDailyMission)
                .where(
                        studyLogDailyMission.dailyMission.id.in(
                                JPAExpressions.select(dailyMission.id)
                                        .from(dailyMission)
                                        .where(dailyMission.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByDeletedStudyLogOwner() {
        return queryFactory
                .delete(studyLogDailyMission)
                .where(
                        studyLogDailyMission.studyLog.id.in(
                                JPAExpressions.select(studyLog.id)
                                        .from(studyLog)
                                        .where(studyLog.deletedAt.isNotNull())))
                .execute();
    }

    @Override
    public long deleteAllByMemberId(Long memberId) {
        List<Long> ids =
                queryFactory
                        .select(studyLogDailyMission.id)
                        .from(studyLogDailyMission)
                        .join(studyLogDailyMission.studyLog, studyLog)
                        .where(studyLog.member.id.eq(memberId))
                        .fetch();

        if (ids.isEmpty()) return 0;

        return queryFactory
                .delete(studyLogDailyMission)
                .where(studyLogDailyMission.id.in(ids))
                .execute();
    }
}
