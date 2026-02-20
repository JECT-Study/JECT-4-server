package com.ject.studytrip.trip.domain.factory

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import java.time.LocalDate

object TripFactory {
    fun create(
        member: Member,
        name: String,
        memo: String?,
        category: TripCategory,
        endDate: LocalDate?,
        totalStamps: Int,
    ): Trip = Trip.of(member, name, memo, category, endDate, totalStamps)
}
