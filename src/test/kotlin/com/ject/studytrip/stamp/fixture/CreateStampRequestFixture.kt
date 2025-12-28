package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import java.time.LocalDate

class CreateStampRequestFixture {
    var name: String = "TEST 스탬프 이름"
    var endDate: LocalDate? = LocalDate.now().plusDays(7)

    fun withName(name: String): CreateStampRequestFixture = apply { this.name = name }

    fun withEndDate(endDate: LocalDate?): CreateStampRequestFixture = apply { this.endDate = endDate }

    fun withEndDateInPast(): CreateStampRequestFixture = apply { this.endDate = LocalDate.now().minusDays(1) }

    fun withEndDateAfterTripEndDate(): CreateStampRequestFixture = apply { this.endDate = LocalDate.now().plusDays(100) }

    fun build() = CreateStampRequest(name, endDate)
}
