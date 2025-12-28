package com.ject.studytrip.trip.domain.model;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TripCategory {
    COURSE("코스형"),
    EXPLORE("탐험형"),
    ;

    private final String value;

    public String getValue() {
        return this.value;
    }

    public static TripCategory from(String name) {
        return Arrays.stream(TripCategory.values())
                .filter(category -> category.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new CustomException(TripErrorCode.INVALID_TRIP_CATEGORY));
    }
}
