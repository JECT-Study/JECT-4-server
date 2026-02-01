package com.ject.studytrip.studylog.helper

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.domain.repository.StudyLogRepository
import com.ject.studytrip.studylog.fixture.StudyLogFixture
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.stereotype.Component

@Component
class StudyLogTestHelper(
    private val studyLogRepository: StudyLogRepository,
) {
    fun saveStudyLog(
        member: Member,
        dailyGoal: DailyGoal,
    ): StudyLog = studyLogRepository.save(StudyLogFixture(member, dailyGoal).create())

    fun saveDeletedStudyLog(
        member: Member,
        dailyGoal: DailyGoal,
    ): StudyLog = studyLogRepository.save(StudyLogFixture(member, dailyGoal).create().also { it.updateDeletedAt() })
}
