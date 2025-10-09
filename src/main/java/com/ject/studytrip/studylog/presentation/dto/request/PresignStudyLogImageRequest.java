package com.ject.studytrip.studylog.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record PresignStudyLogImageRequest(
        @Schema(description = "원본 이미지 파일명") @NotEmpty(message = "원본 이미지 파일명은 필수 요청 값입니다.")
                String originFilename) {}
