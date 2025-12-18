package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.studylog.domain.factory.StudyLogDailyMissionFactory
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionCommandRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogDailyMissionRepository
import org.springframework.stereotype.Service

@Service
class StudyLogDailyMissionCommandService(
    private val studyLogDailyMissionRepository: StudyLogDailyMissionRepository,
    private val studyLogDailyMissionCommandRepository: StudyLogDailyMissionCommandRepository,
) {
    fun createStudyLogDailyMissions(
        studyLog: StudyLog,
        dailyMissions: List<DailyMission>,
    ): List<StudyLogDailyMission> {
        val studyLogDailyMissions = dailyMissions.map { StudyLogDailyMissionFactory.create(studyLog, it) }

        return studyLogDailyMissionRepository.saveAll(studyLogDailyMissions)
    }

    fun hardDeleteStudyLogDailyMissions(): Long = studyLogDailyMissionCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission(): Long =
        studyLogDailyMissionCommandRepository.deleteAllByDeletedDailyMissionOwner()

    fun hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog(): Long =
        studyLogDailyMissionCommandRepository.deleteAllByDeletedStudyLogOwner()

    fun hardDeleteStudyLogDailyMissionsOwnedByMember(memberId: Long): Long =
        studyLogDailyMissionCommandRepository.deleteAllByMemberId(memberId)
}
