package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import java.time.LocalDate

class CreateStampRequestFixture(
    private val name: String = "TEST 스탬프 이름",
    private val endDate: LocalDate? = LocalDate.now().plusDays(7),
) {
    fun withName(name: String): CreateStampRequestFixture = CreateStampRequestFixture(name, endDate)

    fun withEndDate(endDate: LocalDate?): CreateStampRequestFixture = CreateStampRequestFixture(name, endDate)

    fun withEndDateAfterTripEndDate(): CreateStampRequestFixture = CreateStampRequestFixture(name, LocalDate.now().plusDays(100))

    fun build() = CreateStampRequest(name, endDate)
}
