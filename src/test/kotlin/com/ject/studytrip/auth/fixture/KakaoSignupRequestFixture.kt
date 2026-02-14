package com.ject.studytrip.auth.fixture

import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest

class KakaoSignupRequestFixture(
    private val category: String = "STUDENT",
    private val nickname: String = "민우",
) {
    fun build(): KakaoSignupRequest = KakaoSignupRequest(category, nickname)
}
