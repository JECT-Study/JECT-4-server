package com.ject.studytrip.auth.domain.repository

import com.ject.studytrip.auth.application.dto.KakaoSignupProfile
import java.util.Optional

interface KakaoSignupProfileRedisRepository {
    fun saveAndIssueSignupKey(
        socialId: String,
        email: String,
        profileImageUrl: String,
    ): String

    fun findBySignupKey(signupKey: String): Optional<KakaoSignupProfile>

    fun deleteBySignupKey(signupKey: String)
}
