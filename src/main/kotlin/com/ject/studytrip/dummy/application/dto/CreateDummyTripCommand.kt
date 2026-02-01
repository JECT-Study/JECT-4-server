package com.ject.studytrip.dummy.application.dto

import com.ject.studytrip.trip.domain.model.TripCategory
import java.time.LocalDate

data class CreateDummyTripCommand(
    val name: String,
    val memo: String,
    val tripCategory: TripCategory,
    val endDate: LocalDate?,
) {
    companion object {
        @JvmStatic
        fun of(
            name: String,
            memo: String,
            tripCategory: TripCategory,
            endDate: LocalDate?,
        ): CreateDummyTripCommand = CreateDummyTripCommand(name, memo, tripCategory, endDate)
    }
}
