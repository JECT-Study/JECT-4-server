package com.ject.studytrip.member.application.dto

data class CreateMemberCommand(
    val socialId: String,
    val email: String,
    val profileImage: String?,
    val nickname: String,
    val category: String,
) {
    companion object {
        @JvmStatic
        fun of(
            socialId: String,
            email: String,
            profileImage: String?,
            nickname: String,
            category: String,
        ): CreateMemberCommand = CreateMemberCommand(socialId, email, profileImage, nickname, category)
    }
}
