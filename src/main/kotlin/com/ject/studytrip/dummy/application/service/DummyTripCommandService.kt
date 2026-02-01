package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.dummy.application.dto.CreateDummyTripCommand
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.factory.TripFactory
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DummyTripCommandService {
    fun createDummyTrip(
        member: Member,
        category: String,
        count: Int,
    ): Trip {
        val tripCategory = TripCategory.from(category)

        val command =
            when (tripCategory) {
                TripCategory.COURSE -> CreateDummyTripCommand.of("testTrip", "testMemo", tripCategory, LocalDate.now().plusDays(10))
                TripCategory.EXPLORE -> CreateDummyTripCommand.of("testTrip", "testMemo", tripCategory, null)
            }

        return TripFactory.create(member, command.name, command.memo, command.tripCategory, command.endDate, count)
    }
}
