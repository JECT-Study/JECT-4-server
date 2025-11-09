package com.ject.studytrip.dummy.application.service;

import com.ject.studytrip.dummy.application.dto.CreateDummyTripCommand;
import com.ject.studytrip.dummy.application.generator.CreateDummyTripCommandGenerator;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.factory.TripFactory;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import org.springframework.stereotype.Service;

@Service
public class DummyTripCommandService {

    public Trip createDummyTrip(Member member, String category, int count) {
        CreateDummyTripCommand command =
                CreateDummyTripCommandGenerator.of(TripCategory.from(category));

        return TripFactory.create(
                member,
                command.name(),
                command.memo(),
                command.tripCategory(),
                command.endDate(),
                count);
    }
}
