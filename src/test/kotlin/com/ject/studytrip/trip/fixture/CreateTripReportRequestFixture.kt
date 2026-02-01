package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.CreateTripReportRequest

class CreateTripReportRequestFixture {
    var title: String = "TEST 여행 리포트 제목"
    var content: String = "TEST 여행 리포트 내용"
    var startDate: String = "2018.01.01"
    var endDate: String = "2018.01.31"
    var studyLogCount: Long = 10L
    var totalFocusHours: Long = 100L
    var studyDays: Long = 10L
    var imageTitle: String = "TEST 여행 리포트 이미지 제목"
    var studyLogIds: List<Long> = emptyList()

    fun withStudyLogIds(studyLogIds: List<Long>): CreateTripReportRequestFixture = apply { this.studyLogIds = studyLogIds }

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
