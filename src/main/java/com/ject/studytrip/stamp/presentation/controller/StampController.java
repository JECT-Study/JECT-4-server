package com.ject.studytrip.stamp.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.stamp.application.dto.StampDetail;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.application.facade.StampFacade;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.response.CreateStampResponse;
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampDetailResponse;
import com.ject.studytrip.stamp.presentation.dto.response.LoadStampInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stamp", description = "스탬프 API")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Validated
public class StampController {
    private final StampFacade stampFacade;

    @Operation(summary = "스탬프 등록", description = "특정 여행에 새로운 스탬프를 등록합니다.")
    @PostMapping("/{tripId}/stamps")
    public ResponseEntity<StandardResponse> createStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @RequestBody @Valid CreateStampRequest request) {
        StampInfo result = stampFacade.createStamp(Long.valueOf(memberId), tripId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StandardResponse.success(
                                HttpStatus.CREATED.value(), CreateStampResponse.of(result)));
    }

    @Operation(summary = "스탬프 수정", description = "특정 스탬프의 이름을 수정합니다.")
    @PatchMapping("/{tripId}/stamps/{stampId}")
    public ResponseEntity<StandardResponse> updateStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId,
            @RequestBody @Valid UpdateStampRequest request) {
        stampFacade.updateStamp(Long.valueOf(memberId), tripId, stampId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "스탬프 순서 변경", description = "스탬프 순서를 변경합니다. 스탬프 ID 목록을 최종 순서대로 요청합니다.")
    @PutMapping("/{tripId}/stamps/orders")
    public ResponseEntity<StandardResponse> updateStampOrders(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @RequestBody @Valid UpdateStampOrderRequest request) {
        stampFacade.updateStampOrders(Long.valueOf(memberId), tripId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "스탬프 삭제", description = "특정 스탬프를 삭제합니다.")
    @DeleteMapping("/{tripId}/stamps/{stampId}")
    public ResponseEntity<StandardResponse> deleteStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId) {
        stampFacade.deleteStamp(Long.valueOf(memberId), tripId, stampId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "스탬프 목록 조회", description = "특정 여행의 스탬프 목록을 조회합니다.")
    @GetMapping("/{tripId}/stamps")
    public ResponseEntity<StandardResponse> loadStampsByTrip(
            @AuthenticationPrincipal String memberId, @PathVariable Long tripId) {
        List<StampInfo> result = stampFacade.getStampsByTrip(Long.valueOf(memberId), tripId);
        List<LoadStampInfoResponse> response =
                result.stream().map(LoadStampInfoResponse::of).toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "스탬프 상세 조회", description = "특정 여행의 특정 스탬프 상세 정보를 조회합니다.")
    @GetMapping("/{tripId}/stamps/{stampId}")
    public ResponseEntity<StandardResponse> loadStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId) {
        StampDetail result = stampFacade.getStamp(Long.valueOf(memberId), tripId, stampId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoadStampDetailResponse.of(
                                        result.stampInfo(), result.missionInfos())));
    }

    @Operation(summary = "스탬프 완료", description = "특정 스탬프 하위의 모든 미션이 완료된 경우에만 스탬프를 완료합니다.")
    @PatchMapping("/{tripId}/stamps/{stampId}/complete")
    public ResponseEntity<StandardResponse> completeStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId) {
        stampFacade.completeStamp(Long.valueOf(memberId), tripId, stampId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }
}
