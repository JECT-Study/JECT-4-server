package com.ject.studytrip.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @Schema(description = "리프레시 토큰") @NotBlank(message = "리프레시 토큰을 입력해 주세요.")
                String refreshToken) {}
