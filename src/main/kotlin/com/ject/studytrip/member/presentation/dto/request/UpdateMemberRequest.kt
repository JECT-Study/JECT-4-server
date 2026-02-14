package com.ject.studytrip.member.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern

data class UpdateMemberRequest(
    @field:Schema(description = "수정할 멤버 닉네임")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
        message = "닉네임은 특수문자를 제외하고 2~10자 이내로 입력해주세요.",
    )
    val nickname: String?,
    @field:Schema(description = "수정할 멤버 카테고리")
    @field:Pattern(
        regexp = "^(STUDENT|WORKER|FREELANCER|JOBSEEKER)$",
        message = "멤버 카테고리는 STUDENT, WORKER, FREELANCER, JOBSEEKER 중 하나여야 합니다.",
    )
    val category: String?,
)
