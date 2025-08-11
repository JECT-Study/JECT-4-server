package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.test.util.ReflectionTestUtils;

public class StampFixture {
    private static final String STAMP_NAME = "TEST STAMP NAME";

    public static Stamp createStamp(Trip trip, int order) {
        return StampFactory.create(trip, STAMP_NAME, order);
    }

    public static Stamp createStampWithId(Long id, Trip trip, int order) {
        Stamp stamp = StampFactory.create(trip, STAMP_NAME, order);
        ReflectionTestUtils.setField(stamp, "id", id);

        return stamp;
    }
}
