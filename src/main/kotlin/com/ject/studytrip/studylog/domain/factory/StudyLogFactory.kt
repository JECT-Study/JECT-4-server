package com.ject.studytrip.studylog.domain.factory

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.trip.domain.model.DailyGoal

object StudyLogFactory {
    fun create(
        member: Member,
        dailyGoal: DailyGoal,
        content: String,
    ): StudyLog = StudyLog.of(member, dailyGoal, content)
}
