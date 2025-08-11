package com.ject.studytrip.stamp.domain.factory;

import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StampFactory {
    public static Stamp create(Trip trip, String name, int stampOrder) {
        return Stamp.of(trip, name, stampOrder);
    }
}
