package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.test.util.ReflectionTestUtils;

public class StampFixture {
    private static final String STAMP_NAME = "TEST STAMP NAME";
    private static final java.time.LocalDate DEFAULT_END_DATE =
            java.time.LocalDate.now().plusDays(7);

    public static Stamp createStamp(Trip trip, int order) {
        return StampFactory.create(trip, STAMP_NAME, order, DEFAULT_END_DATE);
    }

    public static Stamp createStampWithId(Long id, Trip trip, int order) {
        Stamp stamp = StampFactory.create(trip, STAMP_NAME, order, DEFAULT_END_DATE);
        ReflectionTestUtils.setField(stamp, "id", id);

        return stamp;
    }

    public static Stamp createStampWithName(Trip trip, String name, int order) {
        return StampFactory.create(trip, name, order, DEFAULT_END_DATE);
    }
}
