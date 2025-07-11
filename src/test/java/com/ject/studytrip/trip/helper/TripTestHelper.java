package com.ject.studytrip.trip.helper;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import com.ject.studytrip.trip.fixture.TripFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripTestHelper {

    @Autowired private TripRepository tripRepository;

    public Trip saveTrip(Member member, TripCategory category) {
        Trip trip = TripFixture.createTrip(member, category);
        return tripRepository.save(trip);
    }

    public Trip saveDeletedTrip(Member member, TripCategory category) {
        Trip trip = TripFixture.createTrip(member, category);
        trip.updateDeletedAt();
        return tripRepository.save(trip);
    }
}
