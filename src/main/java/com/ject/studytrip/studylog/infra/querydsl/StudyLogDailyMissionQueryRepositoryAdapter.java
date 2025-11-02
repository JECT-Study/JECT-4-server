package com.ject.studytrip.studylog.infra.querydsl;

import static com.ject.studytrip.mission.domain.model.QDailyMission.dailyMission;
import static com.ject.studytrip.mission.domain.model.QMission.mission;
import static com.ject.studytrip.studylog.domain.model.QStudyLogDailyMission.studyLogDailyMission;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyLogDailyMissionQueryRepositoryAdapter
        implements StudyLogDailyMissionQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, List<StudyLogDailyMission>> findStudyLogDailyMissionsGroupedByStudyLogId(
            List<Long> studyLogIds) {
        return queryFactory
                .selectFrom(studyLogDailyMission)
                .join(studyLogDailyMission.dailyMission, dailyMission)
                .fetchJoin()
                .join(dailyMission.mission, mission)
                .fetchJoin()
                .where(studyLogDailyMission.studyLog.id.in(studyLogIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(sldm -> sldm.getStudyLog().getId()));
    }
}
