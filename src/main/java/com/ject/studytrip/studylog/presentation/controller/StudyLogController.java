package com.ject.studytrip.studylog.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.studylog.application.dto.StudyLogDetail;
import com.ject.studytrip.studylog.application.dto.StudyLogInfo;
import com.ject.studytrip.studylog.application.facade.StudyLogFacade;
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest;
import com.ject.studytrip.studylog.presentation.dto.response.CreateStudyLogResponse;
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "StudyLog", description = "학습 로그 API")
@RestController
@RequiredArgsConstructor
@Validated
public class StudyLogController {
    private final StudyLogFacade studyLogFacade;

    @Operation(summary = "학습 로그 생성", description = "학습을 완료한 데일리 미션을 선택해 학습 로그를 생성하는 API 입니다.")
    @PostMapping("/api/trips/{tripId}/daily-goals/{dailyGoalId}/study-logs")
    public ResponseEntity<StandardResponse> createStudyLog(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") Long dailyGoalId,
            @RequestBody @Valid CreateStudyLogRequest request) {
        StudyLogInfo result =
                studyLogFacade.createStudyLog(Long.valueOf(memberId), tripId, dailyGoalId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StandardResponse.success(
                                HttpStatus.CREATED.value(), CreateStudyLogResponse.of(result)));
    }

    @Operation(
            summary = "여행의 학습 로그 목록 조회",
            description = "특정 여행의 학습 로그 목록을 조회하는 API 입니다. 슬라이스를 적용하고 최신순으로 정렬합니다.")
    @GetMapping("/api/trips/{tripId}/study-logs")
    public ResponseEntity<StandardResponse> loadStudyLogsByTrip(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "5") @Min(1) @Max(10) int size) {
        Slice<StudyLogDetail> result =
                studyLogFacade.getStudyLogsByTrip(Long.valueOf(memberId), tripId, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(), LoadStudyLogsSliceResponse.of(result)));
    }
}
