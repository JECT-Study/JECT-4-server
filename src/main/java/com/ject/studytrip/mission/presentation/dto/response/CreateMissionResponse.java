package com.ject.studytrip.mission.presentation.dto.response;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateMissionResponse(@Schema(description = "미션 ID") Long missionId) {
    public static CreateMissionResponse of(MissionInfo info) {
        return new CreateMissionResponse(info.missionId());
    }
}
