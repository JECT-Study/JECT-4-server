package com.ject.studytrip.trip.fixture

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import java.time.LocalDate

class CreateTripRequestFixture {
    var name: String = "TEST 여행 이름"
    var memo: String = "TEST 여행 메모"
    var category: String = TripCategory.COURSE.name
    var endDate: LocalDate? = LocalDate.now().plusDays(10)
    var stamps: List<CreateStampRequest> = listOf(CreateStampRequest("TEST 스탬프 이름", LocalDate.now().plusDays(5)))

    fun withCategory(category: String): CreateTripRequestFixture = apply { this.category = category }

    fun withEndDate(endDate: LocalDate?): CreateTripRequestFixture = apply { this.endDate = endDate }

    fun build(): CreateTripRequest = CreateTripRequest(name, memo, category, endDate, stamps)
}
