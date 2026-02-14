package com.ject.studytrip.auth.fixture

import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest

class KakaoLoginRequestFixture(
    private val code: String = "kakao-auth-code-1234567890",
) {
    fun build(): KakaoLoginRequest = KakaoLoginRequest(code)
}
