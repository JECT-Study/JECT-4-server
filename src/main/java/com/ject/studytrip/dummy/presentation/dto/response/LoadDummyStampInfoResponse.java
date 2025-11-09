package com.ject.studytrip.dummy.presentation.dto.response;

import com.ject.studytrip.dummy.application.dto.DummyStampInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadDummyStampInfoResponse(
        @Schema(description = "스탬프 ID") Long stampId,
        @Schema(description = "스탬프 이름") String stampName,
        @Schema(description = "스탬프 순서") int stampOrder,
        @Schema(description = "스탬프 종료일") String endDate,
        @Schema(description = "스탬프에 속한 총 미션 개수") int totalMissions,
        @Schema(description = "스탬프에 속한 완료된 미션 개수") int completedMissions,
        @Schema(description = "스탬프 완료 여부") boolean completed) {
    public static LoadDummyStampInfoResponse of(DummyStampInfo info) {
        return new LoadDummyStampInfoResponse(
                info.stampId(),
                info.stampName(),
                info.stampOrder(),
                info.endDate(),
                info.totalMissions(),
                info.completedMissions(),
                info.completed());
    }
}
