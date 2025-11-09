package com.ject.studytrip.dummy.application.generator;

import com.ject.studytrip.dummy.application.dto.CreateDummyStampCommand;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;

public final class CreateDummyStampCommandGenerator {
    private CreateDummyStampCommandGenerator() {}

    public static CreateDummyStampCommand of(TripCategory tripCategory, int stampOrder) {
        return switch (tripCategory) {
            case COURSE -> ofCourse(stampOrder);
            case EXPLORE -> ofExplore();
        };
    }

    private static CreateDummyStampCommand ofCourse(int stampOrder) {
        return CreateDummyStampCommand.of("testStamp", stampOrder, LocalDate.now().plusDays(10));
    }

    private static CreateDummyStampCommand ofExplore() {
        return CreateDummyStampCommand.of("testStamp", 0, null);
    }
}
