package com.ject.studytrip.auth.application.dto

data class KakaoSignupProfile(
    val socialId: String,
    val socialProvider: String,
    val email: String,
    val profileImageUrl: String?,
)
