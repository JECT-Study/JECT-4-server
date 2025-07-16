package com.ject.studytrip.mission.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateMissionRequest(
        @Schema(description = "수정할 미션 이름") String name,
        @Schema(description = "수정할 미션 메모") String memo) {}
