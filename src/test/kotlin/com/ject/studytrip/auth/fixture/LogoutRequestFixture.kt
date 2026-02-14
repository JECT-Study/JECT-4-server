package com.ject.studytrip.auth.fixture

import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest

class LogoutRequestFixture(
    private val accessToken: String = "logout-access-token",
) {
    fun withAccessToken(accessToken: String): LogoutRequestFixture = LogoutRequestFixture(accessToken)

    fun build(): LogoutRequest = LogoutRequest(accessToken)
}
