package com.ject.studytrip.dummy.presentation.controller;

import com.ject.studytrip.dummy.application.dto.DummyMissionsInfo;
import com.ject.studytrip.dummy.application.facade.DummyMissionFacade;
import com.ject.studytrip.dummy.presentation.dto.response.LoadDummyMissionInfoResponse;
import com.ject.studytrip.global.common.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dummy Mission", description = "더미 미션 API")
@RestController
@RequestMapping("/api/dummies/missions")
@RequiredArgsConstructor
@Validated
public class DummyMissionController {
    private final DummyMissionFacade dummyMissionFacade;

    @Operation(
            summary = "더미 미션 목록 조회",
            description = "여행 카테고리와 생성할 더미 데이터 개수를 이용해 더미 미션 목록을 조회합니다.(DB 저장 X)")
    @GetMapping
    public ResponseEntity<StandardResponse> loadDummyMissions(
            @AuthenticationPrincipal String memberId,
            @RequestParam
                    @NotNull(message = "여행 카테고리는 필수 요청 값입니다.")
                    @Pattern(
                            regexp = "^(COURSE|EXPLORE)$",
                            message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.")
                    String category,
            @RequestParam @Min(value = 1, message = "count는 1 이상이어야 합니다.") int count) {
        DummyMissionsInfo result =
                dummyMissionFacade.generateDummyMissions(Long.valueOf(memberId), category, count);
        List<LoadDummyMissionInfoResponse> responses =
                result.missionInfos().stream().map(LoadDummyMissionInfoResponse::of).toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), responses));
    }
}
