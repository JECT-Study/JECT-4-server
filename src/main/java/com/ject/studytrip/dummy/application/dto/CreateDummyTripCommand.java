package com.ject.studytrip.dummy.application.dto;

import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;

public record CreateDummyTripCommand(
        String name, String memo, TripCategory tripCategory, LocalDate endDate) {
    public static CreateDummyTripCommand of(
            String name, String memo, TripCategory tripCategory, LocalDate endDate) {
        return new CreateDummyTripCommand(name, memo, tripCategory, endDate);
    }
}
