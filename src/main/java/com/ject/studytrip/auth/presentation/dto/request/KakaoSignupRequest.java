package com.ject.studytrip.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record KakaoSignupRequest(
        @Schema(description = "멤버 카테고리")
                @NotBlank(message = "멤버 카테고리를 입력해 주세요.")
                @Pattern(
                        regexp = "^(STUDENT|WORKER|FREELANCER|JOBSEEKER)$",
                        message = "멤버 카테고리는 STUDENT, WORKER, FREELANCER, JOBSEEKER 중 하나여야 합니다.")
                String category,
        @Schema(description = "닉네임")
                @NotBlank(message = "닉네임을 입력해 주세요.")
                @Pattern(
                        regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
                        message = "닉네임은 특수문자를 제외하고 2~10자 이내로 입력해주세요.")
                String nickname) {}
