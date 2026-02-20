package com.ject.studytrip.studylog.fixture

import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest

class CreateStudyLogRequestFixture(
    private val totalFocusTimeInSeconds: Int = 60,
    private val selectedDailyMissionIds: List<Long> = emptyList(),
    private val content: String = "TEST 학습 로그 내용",
) {
    fun withTotalFocusTimeInMinutes(minutes: Int): CreateStudyLogRequestFixture =
        CreateStudyLogRequestFixture(minutes * 60, selectedDailyMissionIds, content)

    fun withSelectedDailyMissionIds(ids: List<Long>): CreateStudyLogRequestFixture =
        CreateStudyLogRequestFixture(totalFocusTimeInSeconds, ids.toList(), content)

    fun build(): CreateStudyLogRequest = CreateStudyLogRequest(totalFocusTimeInSeconds, selectedDailyMissionIds, content)
}
