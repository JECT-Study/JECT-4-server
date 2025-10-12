package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo;
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse;
import com.ject.studytrip.trip.application.dto.TripReportInfo;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadTripReportDetailResponse(
        @Schema(description = "여행 리포트 ID") Long tripReportId,
        @Schema(description = "여행 리포트 제목") String title,
        @Schema(description = "여행 리포트 내용") String content,
        @Schema(description = "여행 시작일 (여행 회고)") String startDate,
        @Schema(description = "여행 종료일 (여행 회고)") String endDate,
        @Schema(description = "총 학습 시간") long totalFocusHours,
        @Schema(description = "완료된 미션 수 (세션 성공)") long completedMissionCount,
        @Schema(description = "연속 학습일") long studyDays,
        @Schema(description = "여행 리포트 이미지 제목") String imageTitle,
        @Schema(description = "여행 리포트 이미지 URL") String imageUrl,
        @Schema(description = "학습 로그 히스토리") LoadStudyLogsSliceResponse history) {
    public static LoadTripReportDetailResponse of(
            TripReportInfo tripReportInfo, StudyLogSliceInfo studyLogSliceInfo) {
        return new LoadTripReportDetailResponse(
                tripReportInfo.tripReportId(),
                tripReportInfo.title(),
                tripReportInfo.content(),
                tripReportInfo.startDate(),
                tripReportInfo.endDate(),
                tripReportInfo.totalFocusHours(),
                tripReportInfo.completedMissionCount(),
                tripReportInfo.studyDays(),
                tripReportInfo.imageTitle(),
                tripReportInfo.imageUrl(),
                LoadStudyLogsSliceResponse.of(
                        studyLogSliceInfo.studyLogDetails(), studyLogSliceInfo.hasNext()));
    }
}
