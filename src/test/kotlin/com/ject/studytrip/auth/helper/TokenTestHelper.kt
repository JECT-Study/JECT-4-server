package com.ject.studytrip.auth.helper

import com.ject.studytrip.auth.infra.provider.TokenProvider
import org.springframework.stereotype.Component

@Component
class TokenTestHelper(
    private val tokenProvider: TokenProvider,
) {
    fun createAccessToken(
        memberId: String,
        role: String,
    ): String = tokenProvider.createAccessToken(memberId, role)

    fun createRefreshToken(): String = tokenProvider.createRefreshToken()
}
