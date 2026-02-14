package com.ject.studytrip.member.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class PresignProfileImageResponse(
    @field:Schema(description = "멤버 ID")
    val memberId: Long,
    @field:Schema(description = "멤버 프로필 이미지 임시키")
    val tmpKey: String,
    @field:Schema(description = "멤버 프로필 이미지 업로드용 Presigned URL")
    val presignedUrl: String,
) {
    companion object {
        @JvmStatic
        fun of(
            memberId: Long,
            tmpKey: String,
            presignedUrl: String,
        ): PresignProfileImageResponse = PresignProfileImageResponse(memberId, tmpKey, presignedUrl)
    }
}
