package com.ject.studytrip.trip.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UpdateDailyGoalRequest(
        @Schema(name = "삭제할 데일리 미션 ID 목록") List<Long> deleteDailyMissionIds,
        @Schema(name = "추가할 미션 ID 목록") List<Long> addMissionIds) {}
