package com.ject.studytrip.trip.presentation.controller

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.trip.application.facade.TripFacade
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import com.ject.studytrip.trip.presentation.dto.response.CreateTripResponse
import com.ject.studytrip.trip.presentation.dto.response.LoadTripCategoryResponse
import com.ject.studytrip.trip.presentation.dto.response.LoadTripDetailResponse
import com.ject.studytrip.trip.presentation.dto.response.LoadTripsSliceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Trip", description = "여행 API")
@RestController
@RequestMapping("/api/trips")
@Validated
class TripController(
    private val tripFacade: TripFacade,
) {
    @Operation(summary = "여행 생성", description = "새로운 여행을 생성합니다. 여행을 생성하는 동시에 스탬프를 1개 이상 함께 생성합니다.")
    @PostMapping
    fun createTrip(
        @AuthenticationPrincipal memberId: String,
        @RequestBody @Valid request: CreateTripRequest,
    ): ResponseEntity<StandardResponse> {
        val result = tripFacade.createTrip(memberId.toLong(), request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(HttpStatus.CREATED.value(), CreateTripResponse.of(result)))
    }

    @Operation(summary = "여행 수정", description = "특정 여행을 수정합니다.")
    @PatchMapping("/{tripId}")
    fun updateTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @RequestBody @Valid request: UpdateTripRequest,
    ): ResponseEntity<StandardResponse> {
        tripFacade.updateTrip(memberId.toLong(), tripId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "여행 삭제", description = "특정 여행을 삭제합니다.")
    @DeleteMapping("/{tripId}")
    fun deleteTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
    ): ResponseEntity<StandardResponse> {
        tripFacade.deleteTrip(memberId.toLong(), tripId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "여행 완료", description = "특정 여행의 모든 스탬프가 완료된 경우에만 여행을 완료합니다.")
    @PatchMapping("/{tripId}/complete")
    fun completeTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
    ): ResponseEntity<StandardResponse> {
        tripFacade.completeTrip(memberId.toLong(), tripId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "여행 카테고리 목록 조회", description = "여행 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    fun loadTripCategories(): ResponseEntity<StandardResponse> {
        val result = tripFacade.loadTripCategories()
        val responses = result.map(LoadTripCategoryResponse::of)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), responses))
    }

    @Operation(summary = "특정 멤버의 여행 목록을 조회합니다. 슬라이스를 적용하고 D-DAY 정보가 이른 순으로 정렬합니다")
    @GetMapping
    fun loadTrips(
        @AuthenticationPrincipal memberId: String,
        @RequestParam(name = "page", defaultValue = "0") @Min(0) page: Int,
        @RequestParam(name = "size", defaultValue = "5") @Min(1) @Max(10) size: Int,
    ): ResponseEntity<StandardResponse> {
        val result = tripFacade.getTripsByMember(memberId.toLong(), page, size)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), LoadTripsSliceResponse.of(result.tripInfos, result.hasNext)))
    }

    @Operation(summary = "여행 상세 조회", description = "특정 여행을 상세 조회합니다.")
    @GetMapping("/{tripId}")
    fun loadTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
    ): ResponseEntity<StandardResponse> {
        val result = tripFacade.getTrip(memberId.toLong(), tripId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), LoadTripDetailResponse.of(result.tripInfo, result.stampInfos)))
    }
}
