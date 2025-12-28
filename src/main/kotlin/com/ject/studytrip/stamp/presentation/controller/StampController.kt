package com.ject.studytrip.stamp.presentation.controller

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.stamp.application.facade.StampFacade
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import com.ject.studytrip.stamp.presentation.dto.response.CreateStampResponse
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampDetailResponse
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampInfoResponse
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Stamp", description = "스탬프 API")
@RestController
@RequestMapping("/api/trips/{tripId}/stamps")
@Validated
class StampController(
    private val stampFacade: StampFacade,
) {
    @Operation(summary = "스탬프 생성", description = "특정 여행에 새로운 스탬프를 생성합니다.")
    @PostMapping
    fun createStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @RequestBody @Valid request: CreateStampRequest,
    ): ResponseEntity<StandardResponse> {
        val result = stampFacade.createStamp(memberId.toLong(), tripId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(HttpStatus.CREATED.value(), CreateStampResponse.of(result)))
    }

    @Operation(summary = "스탬프 수정", description = "특정 스탬프를 수정합니다. 종료일을 '없음'으로 변경하려면 요청 바디를 생략해서 전달해야 합니다.")
    @PatchMapping("/{stampId}")
    fun updateStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
        @RequestBody @Valid request: UpdateStampRequest,
    ): ResponseEntity<StandardResponse> {
        stampFacade.updateStamp(memberId.toLong(), tripId, stampId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "스탬프 순서 변경", description = "특정 스탬프의 순서를 변경합니다.")
    @PutMapping("/orders")
    fun updateStampOrders(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @RequestBody @Valid request: UpdateStampOrderRequest,
    ): ResponseEntity<StandardResponse> {
        stampFacade.updateStampOrders(memberId.toLong(), tripId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "스탬프 삭제", description = "특정 스탬프를 삭제합니다.")
    @DeleteMapping("/{stampId}")
    fun deleteStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
    ): ResponseEntity<StandardResponse> {
        stampFacade.deleteStamp(memberId.toLong(), tripId, stampId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "스탬프 완료", description = "특정 스탬프의 모든 미션이 완료된 경우에만 스탬프를 완료합니다")
    @PatchMapping("/{stampId}/complete")
    fun completeStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
    ): ResponseEntity<StandardResponse> {
        stampFacade.completeStamp(memberId.toLong(), tripId, stampId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "스탬프 목록 조회", description = "특정 여행의 스탬프 목록을 조회합니다.")
    @GetMapping
    fun loadStampsByTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
    ): ResponseEntity<StandardResponse> {
        val result = stampFacade.getStampsByTrip(memberId.toLong(), tripId)
        val responses = result.stampInfos.map { LoadStampInfoResponse.of(it) }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), responses))
    }

    @Operation(summary = "스탬프 상세 조회", description = "특정 스탬프를 상세 조회합니다.")
    @GetMapping("/{stampId}")
    fun loadStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
    ): ResponseEntity<StandardResponse> {
        val result = stampFacade.getStamp(memberId.toLong(), tripId, stampId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), LoadStampDetailResponse.of(result.stampInfo, result.missionInfos)))
    }
}
