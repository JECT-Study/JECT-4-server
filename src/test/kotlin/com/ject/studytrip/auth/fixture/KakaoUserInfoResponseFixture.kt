package com.ject.studytrip.auth.fixture

import com.ject.studytrip.auth.infra.dto.KakaoAccount
import com.ject.studytrip.auth.infra.dto.KakaoProfile
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse

class KakaoUserInfoResponseFixture(
    private val kakaoId: String = "12345",
    private val email: String = "studytrip@kakao.com",
    private val profileImage: String = "https://kakao.com/profile.jpg",
) {
    fun build(): KakaoUserInfoResponse = KakaoUserInfoResponse(kakaoId, KakaoAccount(KakaoProfile(profileImage), email))
}
