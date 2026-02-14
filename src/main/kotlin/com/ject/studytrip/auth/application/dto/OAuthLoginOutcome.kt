package com.ject.studytrip.auth.application.dto

sealed class OAuthLoginOutcome {
    data class Success(
        val tokenInfo: TokenInfo,
    ) : OAuthLoginOutcome()

    data class SignupRequired(
        val signupKey: String,
    ) : OAuthLoginOutcome()

    companion object {
        @JvmStatic
        fun success(
            accessToken: String,
            refreshToken: String,
            refreshTokenExpiresIn: Long,
        ): OAuthLoginOutcome = Success(TokenInfo(accessToken, refreshToken, refreshTokenExpiresIn))

        @JvmStatic
        fun signupRequired(signupKey: String): OAuthLoginOutcome = SignupRequired(signupKey)
    }
}
