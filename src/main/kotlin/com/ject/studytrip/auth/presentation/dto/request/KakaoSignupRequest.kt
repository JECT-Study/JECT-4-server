package com.ject.studytrip.auth.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class KakaoSignupRequest(
    @field:Schema(description = "멤버 카테고리")
    @field:NotBlank(message = "멤버 카테고리를 입력해 주세요.")
    @field:Pattern(
        regexp = "^(STUDENT|WORKER|FREELANCER|JOBSEEKER)$",
        message = "멤버 카테고리는 STUDENT, WORKER, FREELANCER, JOBSEEKER 중 하나여야 합니다.",
    )
    val category: String,
    @field:Schema(description = "멤버 닉네임")
    @field:NotBlank(message = "멤버 닉네임을 입력해 주세요.")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
        message = "닉네임은 특수문자를 제외하고 2~10자 이내로 입력해주세요.",
    )
    val nickname: String,
)
