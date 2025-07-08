package com.ject.studytrip.trip.presentation.dto.request;

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

public record CreateTripRequest(
        @Schema(description = "여행 이름") @NotEmpty(message = "여행 이름은 필수 요청 값입니다.") String name,
        @Schema(description = "여행 메모") String memo,
        @Schema(description = "여행 카테고리")
                @NotNull(message = "여행 카테고리는 필수 요청 값입니다.")
                @Pattern(
                        regexp = "^(COURSE|EXPLORE)$",
                        message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.")
                String category,
        @Schema(description = "여행 종료일") @FutureOrPresent(message = "여행 종료일은 현재 날짜보다 과거일 수 없습니다.")
                LocalDate endDate,
        @Schema(description = "여행 스탬프 목록") @Valid @NotEmpty(message = "스탬프는 최소 1개 이상 함께 등록해야합니다.")
                List<CreateStampRequest> stamps) {}
