package com.ject.studytrip.stamp.presentation.dto.response;

import com.ject.studytrip.stamp.application.dto.StampInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadStampInfoResponse(
        @Schema(description = "스탬프 ID") Long stampId,
        @Schema(description = "스탬프 이름") String stampName,
        @Schema(description = "스탬프 순서") int stampOrder,
        @Schema(description = "스탬프 종료일") String endDate,
        @Schema(description = "스탬프에 속한 총 미션 개수") int totalMissions,
        @Schema(description = "스탬프에 속한 완료된 미션 개수") int completedMissions,
        @Schema(description = "스탬프 완료 여부") boolean completed) {
    public static LoadStampInfoResponse of(StampInfo info) {
        return new LoadStampInfoResponse(
                info.stampId(),
                info.stampName(),
                info.stampOrder(),
                info.endDate(),
                info.totalMissions(),
                info.completedMissions(),
                info.completed());
    }
}
