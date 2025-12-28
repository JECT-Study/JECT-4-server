package com.ject.studytrip.trip.presentation.controller

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.trip.application.facade.DailyGoalFacade
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest
import com.ject.studytrip.trip.presentation.dto.response.CreateDailyGoalResponse
import com.ject.studytrip.trip.presentation.dto.response.LoadDailyGoalDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "DailyGoal", description = "데일리 목표 API")
@RestController
@RequestMapping("/api/trips/{tripId}/daily-goals")
@Validated
class DailyGoalController(
    private val dailyGoalFacade: DailyGoalFacade,
) {
    @Operation(summary = "데일리 목표 생성", description = "특정 여행에 새로운 데일리 목표를 생성합니다.")
    @PostMapping
    fun createDailyGoal(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @RequestBody @Valid request: CreateDailyGoalRequest,
    ): ResponseEntity<StandardResponse> {
        val result = dailyGoalFacade.createDailyGoal(memberId.toLong(), tripId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(HttpStatus.CREATED.value(), CreateDailyGoalResponse.of(result)))
    }

    @Operation(summary = "데일리 목표 수정", description = "특정 데일리 목표를 수정합니다.")
    @PatchMapping("/{dailyGoalId}")
    fun updateDailyGoal(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") dailyGoalId: Long,
        @RequestBody request: UpdateDailyGoalRequest,
    ): ResponseEntity<StandardResponse> {
        dailyGoalFacade.updateDailyGoal(memberId.toLong(), tripId, dailyGoalId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "데일리 목표 삭제", description = "특정 데일리 목표를 삭제합니다.")
    @DeleteMapping("/{dailyGoalId}")
    fun deleteDailyGaol(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") dailyGoalId: Long,
    ): ResponseEntity<StandardResponse> {
        dailyGoalFacade.deleteDailyGoal(memberId.toLong(), tripId, dailyGoalId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "데일리 목표 상세 조회", description = "특정 데일리 목표를 상세 조회합니다.")
    @GetMapping("/{dailyGoalId}")
    fun loadDailyGoal(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") dailyGoalId: Long,
    ): ResponseEntity<StandardResponse> {
        val result = dailyGoalFacade.getDailyGoal(memberId.toLong(), tripId, dailyGoalId)
        val response = LoadDailyGoalDetailResponse.of(result.dailyGoalInfo, result.pomodoroInfo, result.dailyMissionInfos)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), response))
    }
}
