package com.ject.studytrip.trip.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateTripResponse(@Schema(description = "여행 ID") Long tripId) {
    public static CreateTripResponse of(Long tripId) {
        return new CreateTripResponse(tripId);
    }
}
