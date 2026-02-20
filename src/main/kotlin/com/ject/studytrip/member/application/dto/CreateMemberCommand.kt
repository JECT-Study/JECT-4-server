package com.ject.studytrip.member.application.dto

data class CreateMemberCommand(
    val socialId: String,
    val email: String,
    val profileImage: String?,
    val nickname: String,
    val category: String,
)
