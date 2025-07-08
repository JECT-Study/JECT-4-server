package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import java.time.LocalDate;

public class StampFixture {
    private static final String STAMP_NAME = "TEST STAMP NAME";
    private static final LocalDate STAMP_DEAD_LINE = LocalDate.now().plusDays(7);

    public static Stamp createStamp(Trip trip, int order) {
        return Stamp.of(trip, STAMP_NAME, order, STAMP_DEAD_LINE);
    }
}
