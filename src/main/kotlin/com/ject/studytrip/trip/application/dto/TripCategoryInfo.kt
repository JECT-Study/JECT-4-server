package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.trip.domain.model.TripCategory

data class TripCategoryInfo(
    val name: String,
    val value: String,
) {
    companion object {
        @JvmStatic
        fun from(category: TripCategory): TripCategoryInfo = TripCategoryInfo(category.name, category.getValue())
    }
}
