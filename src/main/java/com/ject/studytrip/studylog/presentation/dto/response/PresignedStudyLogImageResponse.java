package com.ject.studytrip.studylog.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedStudyLogImageResponse(
        @Schema(description = "학습 로그 ID") Long studyLogId,
        @Schema(description = "학습 로그 이미지 임시키") String tmpKey,
        @Schema(description = "학습 로그 이미지 업로드용 Presigned URL") String presignedUrl) {
    public static PresignedStudyLogImageResponse of(
            Long studyLogId, String tmpKey, String presignedUrl) {
        return new PresignedStudyLogImageResponse(studyLogId, tmpKey, presignedUrl);
    }
}
