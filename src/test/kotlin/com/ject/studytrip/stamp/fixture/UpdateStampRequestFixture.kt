package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import java.time.LocalDate

class UpdateStampRequestFixture {
    var name: String? = "TEST 새로운 스탬프 이름"
    var endDate: LocalDate? = LocalDate.now().plusDays(7)

    fun withName(name: String?): UpdateStampRequestFixture = apply { this.name = name }

    fun withEndTime(endTime: LocalDate?): UpdateStampRequestFixture = apply { this.endDate = endTime }

    fun build() = UpdateStampRequest(name, endDate)
}
