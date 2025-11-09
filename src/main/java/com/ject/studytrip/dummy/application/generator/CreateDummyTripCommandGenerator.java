package com.ject.studytrip.dummy.application.generator;

import com.ject.studytrip.dummy.application.dto.CreateDummyTripCommand;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;

public final class CreateDummyTripCommandGenerator {
    private CreateDummyTripCommandGenerator() {}

    public static CreateDummyTripCommand of(TripCategory tripCategory) {
        return switch (tripCategory) {
            case COURSE -> ofCourse();
            case EXPLORE -> ofExplore();
        };
    }

    private static CreateDummyTripCommand ofCourse() {
        return CreateDummyTripCommand.of(
                "testTrip", "testMemo", TripCategory.COURSE, LocalDate.now().plusDays(10));
    }

    private static CreateDummyTripCommand ofExplore() {
        return CreateDummyTripCommand.of("testTrip", "testMemo", TripCategory.EXPLORE, null);
    }
}
