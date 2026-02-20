package com.ject.studytrip.dummy.presentation.controller

import com.ject.studytrip.dummy.application.facade.DummyStampFacade
import com.ject.studytrip.dummy.presentation.dto.response.LoadDummyStampInfoResponse
import com.ject.studytrip.global.common.response.StandardResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Dummy Stamp", description = "더미 스탬프 API")
@RestController
@RequestMapping("/api/dummies/stamps")
@Validated
class DummyStampController(
    private val dummyStampFacade: DummyStampFacade,
) {
    @Operation(
        summary = "더미 스탬프 목록 조회",
        description = "여행 카테고리와 생성할 더미 데이터 개수를 이용해 더미 스탬프 목록을 조회합니다. (DB 저장 X)",
    )
    @GetMapping
    fun loadDummyStamps(
        @AuthenticationPrincipal memberId: String,
        @RequestParam @NotNull(message = "여행 카테고리는 필수 요청 값입니다.")
        @Pattern(
            regexp = "^(COURSE|EXPLORE)$",
            message = "여행 카테고리는 COURSE, EXPLORE 중 하나여야 합니다.",
        )
        category: String,
        @RequestParam @Min(value = 1, message = "count는 1 이상이어야 합니다.") count: Int,
    ): ResponseEntity<StandardResponse> {
        val result = dummyStampFacade.generateDummyStamps(memberId.toLong(), category, count)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), result.stampsInfos.map(LoadDummyStampInfoResponse::of)))
    }
}
