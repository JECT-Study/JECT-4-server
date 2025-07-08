package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.stamp.application.dto.StampInfo;
import java.util.List;

public record TripDetail(TripInfo tripInfo, List<StampInfo> stampInfos) {
    public static TripDetail from(TripInfo tripInfo, List<StampInfo> stampInfos) {
        return new TripDetail(tripInfo, stampInfos);
    }
}
