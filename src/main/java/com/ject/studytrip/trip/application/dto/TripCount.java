package com.ject.studytrip.trip.application.dto;

public record TripCount(long course, long explore) {
    public static TripCount of(long course, long explore) {
        return new TripCount(course, explore);
    }
}
