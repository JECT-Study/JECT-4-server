package com.ject.studytrip.auth.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class KakaoUserInfoResponse(
    @field:Schema(description = "카카오 ID")
    @field:JsonProperty("id")
    val kakaoId: String,
    @field:Schema(description = "카카오 계정")
    @field:JsonProperty("kakao_account")
    val account: KakaoAccount,
) {
    val profileImage: String
        get() = account.profile.profileImage

    val email: String
        get() = account.email
}
