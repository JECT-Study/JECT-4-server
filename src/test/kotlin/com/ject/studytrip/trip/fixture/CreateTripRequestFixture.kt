package com.ject.studytrip.trip.fixture

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import java.time.LocalDate

class CreateTripRequestFixture(
    private val name: String = "TEST 여행 이름",
    private val memo: String = "TEST 여행 메모",
    private val category: String = TripCategory.COURSE.name,
    private val endDate: LocalDate? = LocalDate.now().plusDays(10),
    private val stamps: List<CreateStampRequest> = listOf(CreateStampRequest("TEST 스탬프 이름", LocalDate.now().plusDays(5))),
) {
    fun withCategory(category: String): CreateTripRequestFixture = CreateTripRequestFixture(name, memo, category, endDate, stamps)

    fun withEndDate(endDate: LocalDate?): CreateTripRequestFixture = CreateTripRequestFixture(name, memo, category, endDate, stamps)

    fun build(): CreateTripRequest = CreateTripRequest(name, memo, category, endDate, stamps)
}
