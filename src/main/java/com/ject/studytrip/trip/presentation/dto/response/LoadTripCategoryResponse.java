package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.trip.application.dto.TripCategoryInfo;

public record LoadTripCategoryResponse(String name, String value) {
    public static LoadTripCategoryResponse of(TripCategoryInfo info) {
        return new LoadTripCategoryResponse(info.name(), info.value());
    }
}
