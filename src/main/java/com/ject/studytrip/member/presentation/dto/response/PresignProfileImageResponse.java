package com.ject.studytrip.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignProfileImageResponse(
        @Schema(description = "멤버 ID") Long memberId,
        @Schema(description = "멤버 프로필 이미지 임시키") String tmpKey,
        @Schema(description = "멤버 프로필 이미지 업로드용 Presigned URL") String presignedUrl) {
    public static PresignProfileImageResponse of(
            Long memberId, String tmpKey, String presignedUrl) {
        return new PresignProfileImageResponse(memberId, tmpKey, presignedUrl);
    }
}
