package com.ject.studytrip.mission.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.application.facade.MissionFacade;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionOrderRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.response.CreateMissionResponse;
import com.ject.studytrip.mission.presentation.dto.response.LoadMissionInfoResponse;
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

@Tag(name = "Mission", description = "미션 API")
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Validated
public class MissionController {
    private final MissionFacade missionFacade;

    @Operation(summary = "미션 생성", description = "특정 스탬프에 새로운 미션을 생성합니다.")
    @PostMapping("/trips/{tripId}/stamps/{stampId}/missions")
    public ResponseEntity<StandardResponse> createMission(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId,
            @RequestBody @Valid CreateMissionRequest request) {
        MissionInfo result =
                missionFacade.createMission(Long.valueOf(memberId), tripId, stampId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StandardResponse.success(
                                HttpStatus.CREATED.value(), CreateMissionResponse.of(result)));
    }

    @Operation(summary = "미션 수정", description = "특정 미션의 이름 또는 메모를 수정합니다.")
    @PatchMapping("/trips/{tripId}/stamps/{stampId}/missions/{missionId}")
    public ResponseEntity<StandardResponse> updateMission(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId,
            @PathVariable @NotNull(message = "미션 ID는 필수 요청 파라미터입니다.") Long missionId,
            @RequestBody UpdateMissionRequest request) {
        missionFacade.updateMissionNameAndMemo(
                Long.valueOf(memberId), tripId, stampId, missionId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(
            summary = "미션 순서 변경",
            description = "코스형과 탐험형 스탬프 모두 미션 순서를 가지며, 요청된 미션 ID 목록의 순서대로 미션 순서를 변경합니다.")
    @PutMapping("/trips/{tripId}/stamps/{stampId}/missions/orders")
    public ResponseEntity<StandardResponse> updateMissionOrders(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId,
            @RequestBody @Valid UpdateMissionOrderRequest request) {
        missionFacade.updateMissionOrders(Long.valueOf(memberId), tripId, stampId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "미션 삭제", description = "특정 미션을 삭제합니다.")
    @DeleteMapping("/trips/{tripId}/stamps/{stampId}/missions/{missionId}")
    public ResponseEntity<StandardResponse> deleteMission(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId,
            @PathVariable @NotNull(message = "미션 ID는 필수 요청 파라미터입니다.") Long missionId) {
        missionFacade.deleteMission(Long.valueOf(memberId), tripId, stampId, missionId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "미션 목록 조회", description = "특정 스탬프의 미션 목록을 조회합니다.")
    @GetMapping("/trips/{tripId}/stamps/{stampId}/missions")
    public ResponseEntity<StandardResponse> loadMissionsByStamp(
            @AuthenticationPrincipal String memberId,
            @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") Long tripId,
            @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") Long stampId) {
        List<MissionInfo> results =
                missionFacade.getMissionsByStamp(Long.valueOf(memberId), tripId, stampId);
        List<LoadMissionInfoResponse> responses =
                results.stream().map(LoadMissionInfoResponse::of).toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), responses));
    }
}
