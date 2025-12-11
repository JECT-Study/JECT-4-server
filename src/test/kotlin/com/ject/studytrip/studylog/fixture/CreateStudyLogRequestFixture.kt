package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest

class CreateStudyLogRequestFixture {
    var totalFocusTimeInSeconds: Int = 60
    var selectedDailyMissionIds: List<Long> = emptyList()
    var content: String = "TEST 학습 로그 내용"

    fun withTotalFocusTimeInMinutes(minutes: Int): CreateStudyLogRequestFixture = apply { this.totalFocusTimeInSeconds = minutes * 60 }

    fun withSelectedDailyMissionIds(ids: List<Long>): CreateStudyLogRequestFixture = apply { this.selectedDailyMissionIds = ids.toList() }

    fun build(): CreateStudyLogRequest =
        CreateStudyLogRequest(
            totalFocusTimeInSeconds,
            selectedDailyMissionIds,
            content,
        )
}
