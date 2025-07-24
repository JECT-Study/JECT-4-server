package com.ject.studytrip.trip.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.trip.application.dto.DailyGoalDetail;
import com.ject.studytrip.trip.application.dto.DailyGoalInfo;
import com.ject.studytrip.trip.application.facade.DailyGoalFacade;
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest;
import com.ject.studytrip.trip.presentation.dto.response.CreateDailyGoalResponse;
import com.ject.studytrip.trip.presentation.dto.response.LoadDailyGoalDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DailyGoal", description = "데일리 목표 API")
@RestController
@RequiredArgsConstructor
@Validated
public class DailyGoalController {
    private final DailyGoalFacade dailyGoalFacade;

    @Operation(summary = "데일리 목표 생성", description = "데일리 목표를 생성하는 API 입니다.")
    @PostMapping("/api/trips/{tripId}/daily-goals")
    public ResponseEntity<StandardResponse> createDailyGoal(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @RequestBody @Valid CreateDailyGoalRequest request) {
        DailyGoalInfo result =
                dailyGoalFacade.createDailyGoal(Long.valueOf(memberId), tripId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StandardResponse.success(
                                HttpStatus.CREATED.value(), CreateDailyGoalResponse.of(result)));
    }

    @Operation(summary = "데일리 목표 수정", description = "데일리 목표를 수정하는 API 입니다.")
    @PatchMapping("/api/trips/{tripId}/daily-goals/{dailyGoalId}")
    public ResponseEntity<StandardResponse> updateDailyGoal(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") Long dailyGoalId,
            @RequestBody UpdateDailyGoalRequest request) {
        dailyGoalFacade.updateDailyGoal(Long.valueOf(memberId), tripId, dailyGoalId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "데일리 목표 삭제", description = "데일리 목표를 삭제하는 API 입니다.")
    @DeleteMapping("/api/trips/{tripId}/daily-goals/{dailyGoalId}")
    public ResponseEntity<StandardResponse> deleteDailyGoal(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") Long dailyGoalId) {
        dailyGoalFacade.deleteDailyGoal(Long.valueOf(memberId), tripId, dailyGoalId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "특정 데일리 목표 조회", description = "특정 데일리 목표를 조회하는 API 입니다.")
    @GetMapping("/api/trips/{tripId}/daily-goals/{dailyGoalId}")
    public ResponseEntity<StandardResponse> loadDailyGoal(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") Long dailyGoalId) {
        DailyGoalDetail result =
                dailyGoalFacade.getDailyGoal(Long.valueOf(memberId), tripId, dailyGoalId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoadDailyGoalDetailResponse.of(
                                        result.dailyGoalInfo(),
                                        result.pomodoroInfo(),
                                        result.dailyMissionInfos())));
    }
}
