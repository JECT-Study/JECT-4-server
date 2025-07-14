package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampInfoResponse;
import com.ject.studytrip.trip.application.dto.TripInfo;
import com.ject.studytrip.trip.domain.model.TripCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadTripDetailResponse(
        @Schema(description = "여행 ID") Long tripId,
        @Schema(description = "여행 이름") String name,
        @Schema(description = "여행 메모") String memo,
        @Schema(description = "여행 카테고리") TripCategory category,
        @Schema(description = "여행 시작일") String startDate,
        @Schema(description = "여행 종료일") String endDate,
        @Schema(description = "D-DAY") Integer dDay,
        @Schema(description = "여행의 총 스탬프 수") int totalStamps,
        @Schema(description = "완료된 총 스탬프 수") int completedStamps,
        @Schema(description = "진행률") Integer progress,
        @Schema(description = "여행 완료 여부") boolean completed,
        @Schema(description = "여행에 속한 스탬프 목록") List<LoadStampInfoResponse> stamps) {
    public static LoadTripDetailResponse of(TripInfo tripInfo, List<StampInfo> stampInfos) {
        return new LoadTripDetailResponse(
                tripInfo.tripId(),
                tripInfo.tripName(),
                tripInfo.tripMemo(),
                tripInfo.tripCategory(),
                tripInfo.startDate(),
                tripInfo.endDate(),
                tripInfo.dDay(),
                tripInfo.totalStamps(),
                tripInfo.completedStamps(),
                tripInfo.progress(),
                tripInfo.completed(),
                stampInfos.stream().map(LoadStampInfoResponse::of).toList());
    }
}
