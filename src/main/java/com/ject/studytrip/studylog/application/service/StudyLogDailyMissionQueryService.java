package com.ject.studytrip.studylog.application.service;

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyLogDailyMissionQueryService {
    private final StudyLogDailyMissionQueryRepository studyLogDailyMissionQueryRepository;

    public Map<Long, List<StudyLogDailyMission>> getGroupedStudyLogDailyMissionsByStudyLogIds(
            List<Long> studyLogIds) {
        return studyLogDailyMissionQueryRepository.findStudyLogDailyMissionsGroupedByStudyLogId(
                studyLogIds);
    }
}
