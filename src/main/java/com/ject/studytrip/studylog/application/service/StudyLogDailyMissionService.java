package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.studylog.domain.factory.StudyLogDailyMissionFactory;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyLogDailyMissionService {
    private final StudyLogDailyMissionRepository studyLogDailyMissionRepository;
    private final StudyLogDailyMissionQueryRepository studyLogDailyMissionQueryRepository;

    public List<StudyLogDailyMission> createStudyLogDailyMissions(
            StudyLog studyLog, List<DailyMission> dailyMissions) {
        List<StudyLogDailyMission> studyLogDailyMissions =
                dailyMissions.stream()
                        .map(
                                dailyMission ->
                                        StudyLogDailyMissionFactory.create(studyLog, dailyMission))
                        .toList();

        return studyLogDailyMissionRepository.saveAll(studyLogDailyMissions);
    }

    public Map<Long, List<StudyLogDailyMission>> getGroupedStudyLogDailyMissionsByStudyLogIds(
            List<Long> studyLogIds) {
        return studyLogDailyMissionQueryRepository.findStudyLogDailyMissionsGroupedByStudyLogId(
                studyLogIds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeleteStudyLogDailyMissions() {
        return studyLogDailyMissionQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission() {
        return studyLogDailyMissionQueryRepository.deleteAllByDeletedDailyMissionOwner();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog() {
        return studyLogDailyMissionQueryRepository.deleteAllByDeletedStudyLogOwner();
    }
}
