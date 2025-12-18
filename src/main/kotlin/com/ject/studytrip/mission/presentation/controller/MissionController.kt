package com.ject.studytrip.mission.presentation.controller

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.mission.application.facade.MissionFacade
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest
import com.ject.studytrip.mission.presentation.dto.response.CreateMissionResponse
import com.ject.studytrip.mission.presentation.dto.response.LoadMissionInfoResponse
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

@Tag(name = "Mission", description = "미션 API")
@RestController
@RequestMapping("/api/trips/{tripId}/stamps/{stampId}/missions")
@Validated
class MissionController(
    private val missionFacade: MissionFacade,
) {
    @Operation(summary = "미션 생성", description = "특정 스탬프에 새로운 미션을 생성합니다.")
    @PostMapping
    fun createMission(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
        @RequestBody @Valid request: CreateMissionRequest,
    ): ResponseEntity<StandardResponse> {
        val result = missionFacade.createMission(memberId.toLong(), tripId, stampId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(HttpStatus.CREATED.value(), CreateMissionResponse.of(result)))
    }

    @Operation(summary = "미션 수정", description = "특정 미션의 이름을 수정합니다.")
    @PatchMapping("/{missionId}")
    fun updateMission(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
        @PathVariable @NotNull(message = "미션 ID는 필수 요청 파라미터입니다.") missionId: Long,
        @RequestBody @Valid request: UpdateMissionRequest,
    ): ResponseEntity<StandardResponse> {
        missionFacade.updateMissionNameIfPresent(memberId.toLong(), tripId, stampId, missionId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "미션 삭제", description = "특정 미션을 삭제합니다.")
    @DeleteMapping("/{missionId}")
    fun deleteMission(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
        @PathVariable @NotNull(message = "미션 ID는 필수 요청 파라미터입니다.") missionId: Long,
    ): ResponseEntity<StandardResponse> {
        missionFacade.deleteMission(memberId.toLong(), tripId, stampId, missionId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), null))
    }

    @Operation(summary = "미션 목록 조회", description = "특정 스탬프의 미션 목록을 조회합니다.")
    @GetMapping
    fun loadMissionsByStamp(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "스탬프 ID는 필수 요청 파라미터입니다.") stampId: Long,
    ): ResponseEntity<StandardResponse> {
        val results = missionFacade.getMissionsByStamp(memberId.toLong(), tripId, stampId)
        val responses = results.missionInfos.map { LoadMissionInfoResponse.of(it) }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), responses))
    }
}
