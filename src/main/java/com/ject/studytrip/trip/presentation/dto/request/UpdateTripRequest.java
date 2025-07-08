package com.ject.studytrip.trip.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateTripRequest(
        @Schema(description = "수정할 여행 이름") @Size(min = 1, message = "여행 이름은 최소 1글자 이상이여야 합니다.")
                String name,
        @Schema(description = "수정할 여행 메모") String memo,
        @Schema(description = "수정할 여행 카테고리")
                @Pattern(
                        regexp = "^(COURSE|EXPLORE)$",
                        message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.")
                String category,
        @Schema(description = "수정할 여행 종료일")
                @FutureOrPresent(message = "여행 종료일은 현재 날짜보다 과거일 수 없습니다.")
                LocalDate endDate) {}
