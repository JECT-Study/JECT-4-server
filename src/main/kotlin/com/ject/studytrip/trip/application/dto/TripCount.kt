package com.ject.studytrip.trip.application.dto

data class TripCount(
    val course: Long,
    val explore: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            course: Long,
            explore: Long,
        ): TripCount = TripCount(course, explore)
    }
}
