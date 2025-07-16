package com.ject.studytrip.mission.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateMissionRequest(
        @Schema(description = "미션 이름") @NotBlank(message = "미션 이름은 필수 요청 값입니다.") String name,
        @Schema(description = "미션 메모") String memo,
        @Schema(description = "미션 순서") @Min(value = 1, message = "모든 미션 순서는 최소 1 이상이어야 합니다.")
                int order) {}
