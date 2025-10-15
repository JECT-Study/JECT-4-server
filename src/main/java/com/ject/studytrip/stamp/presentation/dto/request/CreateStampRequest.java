package com.ject.studytrip.stamp.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;

public record CreateStampRequest(
        @Schema(description = "스탬프 이름") @NotEmpty(message = "스탬프 이름은 필수 요청 값입니다.") String name,
        @Schema(description = "스탬프 순서") @Min(value = 0, message = "스탬프 순서는 최소 0 이상이여야 합니다.")
                int order,
        @Schema(description = "스탬프 종료일") @FutureOrPresent(message = "스탬프 종료일은 현재 날짜보다 과거일 수 없습니다.")
                LocalDate endDate) {}
