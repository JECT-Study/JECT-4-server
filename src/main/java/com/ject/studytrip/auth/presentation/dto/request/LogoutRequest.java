package com.ject.studytrip.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "엑세스 토큰") @NotBlank(message = "엑세스 토큰을 입력해 주세요.")
                String accessToken) {}
