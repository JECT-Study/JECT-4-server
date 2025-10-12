package com.ject.studytrip.trip.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateTripReportRequest(
        @Schema(description = "여행 리포트 제목") @NotEmpty(message = "여행 리포트 제목은 필수 요청 값입니다.")
                String title,
        @Schema(description = "여행 리포트 내용") @NotEmpty(message = "여행 리포트 내용은 필수 요청 값입니다.")
                String content,
        @Schema(description = "여행 시작일") @NotEmpty(message = "여행 시작일은 필수 요청 값입니다.") String startDate,
        @Schema(description = "여행 종료일") String endDate,
        @Schema(description = "완료된 미션 수 (세션 성공)") @NotNull(message = "완료된 미션 수는 필수 요청 값입니다.")
                long completedMissionCount,
        @Schema(description = "총 학습 시간") @NotNull(message = "총 학습 시간은 필수 요청 값입니다.")
                long totalFocusHours,
        @Schema(description = "연속 학습일") @NotNull(message = "연속 학습일은 필수 요청 값입니다.") long studyDays,
        @Schema(description = "이미지 제목") String imageTitle,
        @Schema(description = "학스 로그 ID 목록") @NotEmpty(message = "학습 로그 ID 목록은 최소 1개 이상이어야 합니다.")
                List<@NotNull(message = "학습 로그 ID는 필수 요청 값입니다.") Long> studyLogIds) {}
