package com.ject.studytrip.mission.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateMissionOrderRequest(
        @Schema(description = "변경된 순서를 반영한 미션 ID 목록 (앞에서부터 순서대로 정렬)")
                @NotEmpty(message = "미션 ID 목록은 필수 요청 값입니다.")
                List<@NotNull(message = "미션 ID는 null일 수 없습니다.") Long> orderedMissionIds) {}
