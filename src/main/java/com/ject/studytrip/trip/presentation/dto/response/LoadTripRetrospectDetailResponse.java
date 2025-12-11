package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo;
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse;
import com.ject.studytrip.trip.application.dto.TripInfo;
import com.ject.studytrip.trip.application.dto.TripRetrospectSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadTripRetrospectDetailResponse(
        @Schema(description = "여행 이름") String name,
        @Schema(description = "여행 시작일") String startDate,
        @Schema(description = "여행 종료일") String endDate,
        @Schema(description = "총 학습 시간") long totalFocusHours,
        @Schema(description = "학습 로그 개수 (세션 성공)") long studyLogCount,
        @Schema(description = "연속 학습일") long studyDays,
        @Schema(description = "학습 로그 ID 목록") List<Long> studyLogIds,
        @Schema(description = "학습 로그 히스토리") LoadStudyLogsSliceResponse history) {
    public static LoadTripRetrospectDetailResponse of(
            TripRetrospectSummary tripRetrospectSummary,
            TripInfo tripInfo,
            StudyLogSliceInfo studyLogDetailSlice) {
        return new LoadTripRetrospectDetailResponse(
                tripInfo.tripName(),
                tripInfo.startDate(),
                tripInfo.endDate(),
                tripRetrospectSummary.totalFocusHours(),
                tripRetrospectSummary.studyLogCount(),
                tripRetrospectSummary.studyDays(),
                tripRetrospectSummary.studyLogIds(),
                LoadStudyLogsSliceResponse.of(
                        studyLogDetailSlice.getStudyLogDetails(),
                        studyLogDetailSlice.getHasNext()));
    }
}
