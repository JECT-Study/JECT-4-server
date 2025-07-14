package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.factory.TripFactory;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;
import org.springframework.test.util.ReflectionTestUtils;

public class TripFixture {
    private static final String TRIP_NAME = "TEST TRIP NAME";
    private static final String TRIP_MEMO = "TEST TRIP MEMO";
    private static final TripCategory TRIP_CATEGORY_COURSE = TripCategory.COURSE;
    private static final LocalDate TRIP_END_DATE = LocalDate.now().plusDays(7);
    private static final int TRIP_TOTAL_STAMPS = 1;

    public static Trip createTrip(Member member, TripCategory category) {
        return TripFactory.create(
                member, TRIP_NAME, TRIP_MEMO, category, TRIP_END_DATE, TRIP_TOTAL_STAMPS);
    }

    public static Trip createTripWithId(Long id, Member member, TripCategory category) {
        Trip trip =
                TripFactory.create(
                        member, TRIP_NAME, TRIP_MEMO, category, TRIP_END_DATE, TRIP_TOTAL_STAMPS);
        ReflectionTestUtils.setField(trip, "id", id);

        return trip;
    }

    public static Trip createDeletedTrip(Member member) {
        Trip deleted =
                TripFactory.create(
                        member,
                        TRIP_NAME,
                        TRIP_MEMO,
                        TRIP_CATEGORY_COURSE,
                        TRIP_END_DATE,
                        TRIP_TOTAL_STAMPS);
        deleted.updateDeletedAt();

        return deleted;
    }
}
