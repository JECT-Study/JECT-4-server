package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import java.time.LocalDate

class UpdateStampRequestFixture(
    private val name: String? = "TEST 새로운 스탬프 이름",
    private val endDate: LocalDate? = LocalDate.now().plusDays(7),
) {
    fun withName(name: String?): UpdateStampRequestFixture = UpdateStampRequestFixture(name, endDate)

    fun withEndDate(endDate: LocalDate?): UpdateStampRequestFixture = UpdateStampRequestFixture(name, endDate)

    fun build() = UpdateStampRequest(name, endDate)
}
