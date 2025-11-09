package com.ject.studytrip.dummy.presentation.dto.response;

import com.ject.studytrip.dummy.application.dto.DummyMissionInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadDummyMissionInfoResponse(
        @Schema(description = "미션 ID") Long missionId,
        @Schema(description = "미션 이름") String missionName,
        @Schema(description = "미션 완료여부") boolean completed) {
    public static LoadDummyMissionInfoResponse of(DummyMissionInfo info) {
        return new LoadDummyMissionInfoResponse(
                info.missionId(), info.missionName(), info.completed());
    }
}
