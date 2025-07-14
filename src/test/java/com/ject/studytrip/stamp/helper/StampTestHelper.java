package com.ject.studytrip.stamp.helper;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.fixture.StampFixture;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StampTestHelper {
    @Autowired private StampRepository stampRepository;

    public Stamp saveStamp(Trip trip, int order) {
        Stamp stamp = StampFixture.createStamp(trip, order);
        return stampRepository.save(stamp);
    }

    public Stamp saveDeletedStamp(Trip trip, int order) {
        Stamp stamp = StampFixture.createStamp(trip, order);
        stamp.updateDeletedAt();

        return stampRepository.save(stamp);
    }
}
