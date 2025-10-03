package com.ject.studytrip.trip.application.dto;

public record TripCountInfo(long course, long explore) {
    public static TripCountInfo of(long course, long explore) {
        return new TripCountInfo(course, explore);
    }
}
