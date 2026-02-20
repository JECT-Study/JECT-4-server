package com.ject.studytrip.trip.domain.model

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.trip.domain.error.TripErrorCode

enum class TripCategory(
    val value: String,
) {
    COURSE("코스형"),
    EXPLORE("탐험형"),
    ;

    companion object {
        fun from(name: String): TripCategory =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: throw CustomException(TripErrorCode.INVALID_TRIP_CATEGORY)
    }
}
