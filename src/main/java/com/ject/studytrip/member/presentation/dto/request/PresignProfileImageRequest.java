package com.ject.studytrip.member.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record PresignProfileImageRequest(
        @Schema(description = "업로드할 원본 이미지 파일명") @NotEmpty(message = "원본 이미지 파일명은 필수 요청 값입니다.")
                String originFilename) {}
