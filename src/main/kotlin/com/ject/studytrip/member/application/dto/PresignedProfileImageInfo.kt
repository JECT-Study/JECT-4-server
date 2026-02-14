package com.ject.studytrip.member.application.dto

data class PresignedProfileImageInfo(
    val memberId: Long,
    val tmpKey: String,
    val presignedUrl: String,
) {
    companion object {
        @JvmStatic
        fun of(
            memberId: Long,
            tmpKey: String,
            presignedUrl: String,
        ): PresignedProfileImageInfo = PresignedProfileImageInfo(memberId, tmpKey, presignedUrl)
    }
}
