package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.studylog.domain.factory.StudyLogFactory
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.trip.domain.model.DailyGoal
import org.springframework.test.util.ReflectionTestUtils

class StudyLogFixture(
    private val member: Member,
    private val dailyGoal: DailyGoal,
) {
    var content: String = "TEST 학습 로그 내용"

    fun create(): StudyLog = StudyLogFactory.create(member, dailyGoal, content)

    fun createWithId(id: Long): StudyLog =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
