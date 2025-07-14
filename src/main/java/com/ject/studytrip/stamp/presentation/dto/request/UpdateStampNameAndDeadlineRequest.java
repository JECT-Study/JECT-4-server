package com.ject.studytrip.stamp.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record UpdateStampNameAndDeadlineRequest(
        @Schema(description = "수정할 스탬프 이름") String name,
        @Schema(description = "수정할 스탬프 마감일")
                @FutureOrPresent(message = "스탬프 마감일은 현재 날짜보다 과거일 수 없습니다.")
                LocalDate deadline) {}
