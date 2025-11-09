package com.ject.studytrip.dummy.application.service;

import com.ject.studytrip.dummy.application.dto.CreateDummyStampCommand;
import com.ject.studytrip.dummy.application.generator.CreateDummyStampCommandGenerator;
import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.Trip;
import org.springframework.stereotype.Service;

@Service
public class DummyStampCommandService {

    public Stamp createDummyStamp(Trip trip, int stampOrder) {
        CreateDummyStampCommand command =
                CreateDummyStampCommandGenerator.of(trip.getCategory(), stampOrder);

        return StampFactory.create(trip, command.name(), command.stampOrder(), command.endDate());
    }
}
