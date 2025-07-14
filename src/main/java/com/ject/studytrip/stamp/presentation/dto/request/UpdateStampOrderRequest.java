package com.ject.studytrip.stamp.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UpdateStampOrderRequest(
        @Schema(description = "변경된 순서를 반영한 스탬프 ID 목록 (앞에서부터 순서대로 정렬)")
                List<Long> orderedStampIds) {}
