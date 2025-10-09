package com.ject.studytrip.member.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record ConfirmProfileImageRequest(
        @Schema(description = "업로드된 이미지 임시키") @NotEmpty(message = "업로드된 이미지 임시키는 필수 요청 값입니다.")
                String tmpKey) {}
