package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import java.time.LocalDate

class UpdateTripRequestFixture(
    private val name: String? = null,
    private val memo: String? = null,
    private val category: String? = null,
    private val endDate: LocalDate? = null,
) {
    fun withName(name: String): UpdateTripRequestFixture = UpdateTripRequestFixture(name, memo, category, endDate)

    fun withMemo(memo: String): UpdateTripRequestFixture = UpdateTripRequestFixture(name, memo, category, endDate)

    fun withCategory(category: String): UpdateTripRequestFixture = UpdateTripRequestFixture(name, memo, category, endDate)

    fun withEndDate(endDate: LocalDate): UpdateTripRequestFixture = UpdateTripRequestFixture(name, memo, category, endDate)

    fun build(): UpdateTripRequest = UpdateTripRequest(name, memo, category, endDate)
}
