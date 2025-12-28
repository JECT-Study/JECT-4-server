package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.trip.application.dto.TripCategoryInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadTripCategoryResponse(
    @field:Schema(description = "여행 카테고리 이름")
    val name: String,
    @field:Schema(description = "여행 카테고리 표시 값")
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(tripCategoryInfo: TripCategoryInfo): LoadTripCategoryResponse =
            LoadTripCategoryResponse(tripCategoryInfo.name, tripCategoryInfo.value)
    }
}
