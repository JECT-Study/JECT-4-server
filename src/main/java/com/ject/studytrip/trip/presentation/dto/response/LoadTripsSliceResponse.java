package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.trip.application.dto.TripInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadTripsSliceResponse(
        @Schema(description = "여행 목록") List<TripInfo> tripInfos,
        @Schema(description = "다음 데이터 존재 여부") boolean hasNext) {
    public static LoadTripsSliceResponse of(List<TripInfo> infos, boolean hasNext) {
        return new LoadTripsSliceResponse(infos, hasNext);
    }
}
