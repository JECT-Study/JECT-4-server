package com.ject.studytrip.auth.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    @field:Schema(description = "회원가입 필요 여부")
    val signupRequired: Boolean,
    @field:Schema(description = "엑세스 토큰 (가입된 회원인 경우)")
    val accessToken: String?,
) {
    companion object {
        fun success(accessToken: String): LoginResponse = LoginResponse(false, accessToken)

        fun requiredSignup(): LoginResponse = LoginResponse(true, null)
    }
}
