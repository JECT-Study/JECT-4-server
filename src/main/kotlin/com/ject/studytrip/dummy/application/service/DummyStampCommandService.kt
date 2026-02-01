package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.dummy.application.dto.CreateDummyStampCommand
import com.ject.studytrip.stamp.domain.factory.StampFactory
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DummyStampCommandService {
    fun createDummyStamp(
        trip: Trip,
        stampOrder: Int,
    ): Stamp {
        val command =
            when (trip.category) {
                TripCategory.COURSE -> CreateDummyStampCommand.of("testStamp", stampOrder, LocalDate.now().plusDays(10))
                TripCategory.EXPLORE -> CreateDummyStampCommand.of("testStamp", 0, null)
            }

        return StampFactory.create(trip, command.name, command.stampOrder, command.endDate)
    }
}
