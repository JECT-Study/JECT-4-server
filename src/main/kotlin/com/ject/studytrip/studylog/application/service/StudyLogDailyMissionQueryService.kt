package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionQueryRepository
import org.springframework.stereotype.Service

@Service
class StudyLogDailyMissionQueryService(
    private val studyLogDailyMissionQueryRepository: StudyLogDailyMissionQueryRepository,
) {
    fun getGroupedStudyLogDailyMissionsByStudyLogIds(studyLogIds: List<Long>): Map<Long, List<StudyLogDailyMission>> =
        studyLogDailyMissionQueryRepository.findStudyLogDailyMissionsGroupedByStudyLogId(studyLogIds)
}
