package com.ject.studytrip.studylog.infra.jpa

import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository
import org.springframework.stereotype.Repository

@Repository
class StudyLogDailyMissionRepositoryAdapter(
    private val studyLogDailyMissionJpaRepository: StudyLogDailyMissionJpaRepository,
) : StudyLogDailyMissionRepository {
    override fun saveAll(studyLogDailyMissions: List<StudyLogDailyMission>): List<StudyLogDailyMission> =
        studyLogDailyMissionJpaRepository.saveAll(studyLogDailyMissions)
}
