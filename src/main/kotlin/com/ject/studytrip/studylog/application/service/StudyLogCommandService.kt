package com.ject.studytrip.studylog.application.service

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.studylog.domain.factory.StudyLogFactory
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.policy.StudyLogPolicy
import com.ject.studytrip.studylog.domain.repository.StudyLogCommandRepository
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Service

@Service
class StudyLogCommandService(
    private val studyLogRepository: StudyLogRepository,
    private val studyLogCommandRepository: StudyLogCommandRepository,
) {
    fun createStudyLog(
        member: Member,
        dailyGoal: DailyGoal,
        content: String,
    ): StudyLog {
        val studyLog = StudyLogFactory.create(member, dailyGoal, content)

        return studyLogRepository.save(studyLog)
    }

    fun updateImageUrl(
        studyLog: StudyLog,
        imageUrl: String,
    ) {
        StudyLogPolicy.validateNotDeleted(studyLog)

        studyLog.updateImageUrl(imageUrl)
    }

    fun hardDeleteStudyLogs(): Long = studyLogCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteStudyLogsOwnedByDeletedMember(): Long = studyLogCommandRepository.deleteAllByDeletedMemberOwner()

    fun hardDeleteStudyLogsOwnedByDeletedDailyGoal(): Long = studyLogCommandRepository.deleteAllByDeletedDailyGoalOwner()

    fun hardDeleteStudyLogsOwnedByMember(memberId: Long): Long = studyLogCommandRepository.deleteByMemberId(memberId)
}
