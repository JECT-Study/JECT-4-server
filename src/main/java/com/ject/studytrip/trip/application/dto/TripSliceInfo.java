package com.ject.studytrip.trip.application.dto;

import java.util.List;

public record TripSliceInfo(List<TripInfo> tripInfos, boolean hasNext) {
    public static TripSliceInfo of(List<TripInfo> tripInfos, boolean hasNext) {
        return new TripSliceInfo(tripInfos, hasNext);
    }
}
