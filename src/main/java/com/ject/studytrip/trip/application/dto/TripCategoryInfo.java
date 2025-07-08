package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.trip.domain.model.TripCategory;

public record TripCategoryInfo(String name, String value) {
    public static TripCategoryInfo from(TripCategory category) {
        return new TripCategoryInfo(category.name(), category.getValue());
    }
}
