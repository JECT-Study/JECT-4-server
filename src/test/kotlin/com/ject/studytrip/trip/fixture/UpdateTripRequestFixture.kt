package com.ject.studytrip.trip.fixture

import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import java.time.LocalDate

class UpdateTripRequestFixture {
    private var name: String? = null
    private var memo: String? = null
    private var category: String? = null
    private var endDate: LocalDate? = null

    fun withName(name: String): UpdateTripRequestFixture {
        this.name = name
        return this
    }

    fun withMemo(memo: String): UpdateTripRequestFixture {
        this.memo = memo
        return this
    }

    fun withCategory(category: String): UpdateTripRequestFixture {
        this.category = category
        return this
    }

    fun withEndDate(endDate: LocalDate): UpdateTripRequestFixture {
        this.endDate = endDate
        return this
    }

    fun build(): UpdateTripRequest = UpdateTripRequest(name, memo, category, endDate)
}
