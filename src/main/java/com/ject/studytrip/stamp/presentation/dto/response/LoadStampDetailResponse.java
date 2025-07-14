package com.ject.studytrip.stamp.presentation.dto.response;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.presentation.dto.response.LoadMissionInfoResponse;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadStampDetailResponse(
        @Schema(description = "스탬프 ID") Long stampId,
        @Schema(description = "스탬프 이름") String stampName,
        @Schema(description = "스탬프 순서") int stampOrder,
        @Schema(description = "스탬프 마감일") String stampDeadline,
        @Schema(description = "스탬프 완료 여부") boolean completed,
        @Schema(description = "미션 목록") List<LoadMissionInfoResponse> missions) {
    public static LoadStampDetailResponse of(StampInfo stampInfo, List<MissionInfo> missionInfos) {
        return new LoadStampDetailResponse(
                stampInfo.stampId(),
                stampInfo.stampName(),
                stampInfo.stampOrder(),
                stampInfo.deadline(),
                stampInfo.completed(),
                missionInfos.stream().map(LoadMissionInfoResponse::of).toList());
    }
}
