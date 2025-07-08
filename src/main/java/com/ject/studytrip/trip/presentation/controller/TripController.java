package com.ject.studytrip.trip.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.trip.application.dto.TripCategoryInfo;
import com.ject.studytrip.trip.application.dto.TripDetail;
import com.ject.studytrip.trip.application.dto.TripInfo;
import com.ject.studytrip.trip.application.facade.TripFacade;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import com.ject.studytrip.trip.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trip", description = "여행 API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Validated
public class TripController {

    private final TripFacade tripFacade;

    @Operation(summary = "여행 카테고리 목록 조회", description = "여행 카테고리 목록을 조회하는 API 입니다.")
    @GetMapping("/categories")
    public ResponseEntity<StandardResponse> loadTripCategories() {
        List<TripCategoryInfo> result = tripFacade.loadTripCategories();
        List<LoadTripCategoryResponse> response =
                result.stream().map(LoadTripCategoryResponse::of).toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "여행 생성",
            description = "새로운 여행을 생성하는 API 입니다. 여행을 생성하는 동시에 1개 이상의 스탬프를 함께 생성합니다.")
    @PostMapping
    public ResponseEntity<StandardResponse> createTrip(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid CreateTripRequest request) {
        TripInfo result = tripFacade.createTrip(Long.valueOf(memberId), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StandardResponse.success(
                                HttpStatus.CREATED.value(),
                                CreateTripResponse.of(result.tripId())));
    }

    @Operation(summary = "여행 수정", description = "여행을 수정하는 API 입니다. PATCH 매핑으로 수정을 원하는 필드만 요청합니다.")
    @PatchMapping("/{tripId}")
    public ResponseEntity<StandardResponse> updateTrip(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @RequestBody @Valid UpdateTripRequest request) {
        tripFacade.updateTrip(Long.valueOf(memberId), tripId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "여행 삭제", description = "특정 여행을 삭제하는 API 입니다.")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<StandardResponse> deleteTrip(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId) {
        tripFacade.deleteTrip(Long.valueOf(memberId), tripId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(
            summary = "여행 목록 조회",
            description = "여행 목록을 조회하는 API 입니다. 무한 스크롤을 위해 슬라이스를 적용하고, D-DAY 정보가 이른 순으로 정렬합니다.")
    @GetMapping
    public ResponseEntity<StandardResponse> loadTrips(
            @AuthenticationPrincipal String memberId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "5") @Min(1) @Max(10) int size) {
        Slice<TripInfo> result = tripFacade.getTripsByMember(Long.valueOf(memberId), page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoadTripsSliceResponse.of(result.getContent(), result.hasNext())));
    }

    @Operation(summary = "여행 상세 조회", description = "특정 여행을 조회하는 API 입니다.")
    @GetMapping("/{tripId}")
    public ResponseEntity<StandardResponse> loadTripDetail(
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId) {
        TripDetail result = tripFacade.getTrip(tripId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoadTripDetailResponse.of(result.tripInfo(), result.stampInfos())));
    }
}
