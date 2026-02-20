package com.ject.studytrip.trip.fixture

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.factory.TripReportFactory
import com.ject.studytrip.trip.domain.model.TripReport
import org.springframework.test.util.ReflectionTestUtils

class TripReportFixture(
    private val member: Member,
    private val title: String = "TEST 여행 리포트 제목",
    private val content: String = "TEST 여행 리포트 내용",
    private val startDate: String = "2018.01.01",
    private val endDate: String = "2018.01.31",
    private val studyLogCount: Long = 10L,
    private val totalFocusHours: Long = 100L,
    private val studyDays: Long = 10L,
    private val imageTitle: String = "TEST 여행 리포트 이미지 제목",
) {
    fun create(): TripReport =
        TripReportFactory.create(member, title, content, startDate, endDate, studyLogCount, totalFocusHours, studyDays, imageTitle)

    fun createWithId(id: Long): TripReport = create().also { ReflectionTestUtils.setField(it, "id", id) }
}
