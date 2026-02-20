package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest

class CreateTripReportRequestFixture(
    private val title: String = "TEST 여행 리포트 제목",
    private val content: String = "TEST 여행 리포트 내용",
    private val startDate: String = "2018.01.01",
    private val endDate: String = "2018.01.31",
    private val studyLogCount: Long = 10L,
    private val totalFocusHours: Long = 100L,
    private val studyDays: Long = 10L,
    private val imageTitle: String = "TEST 여행 리포트 이미지 제목",
    private val studyLogIds: List<Long> = emptyList(),
) {
    fun withStudyLogIds(studyLogIds: List<Long>): CreateTripReportRequestFixture =
        CreateTripReportRequestFixture(
            title,
            content,
            startDate,
            endDate,
            studyLogCount,
            totalFocusHours,
            studyDays,
            imageTitle,
            studyLogIds,
        )

    fun build(): CreateTripReportRequest =
        CreateTripReportRequest(
            title,
            content,
            startDate,
            endDate,
            studyLogCount,
            totalFocusHours,
            studyDays,
            imageTitle,
            studyLogIds,
        )
}
