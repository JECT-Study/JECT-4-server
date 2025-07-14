package com.ject.studytrip.mission.presentation.dto.response;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadMissionInfoResponse(
        @Schema(description = "미션 ID") Long missionId,
        @Schema(description = "미션 이름") String missionName,
        @Schema(description = "미션 순서") int missionOrder,
        @Schema(description = "미션 완료여부") boolean completed) {
    public static LoadMissionInfoResponse of(MissionInfo info) {
        return new LoadMissionInfoResponse(
                info.missionId(), info.missionName(), info.missionOrder(), info.completed());
    }
}
