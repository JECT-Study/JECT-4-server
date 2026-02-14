package com.ject.studytrip.auth.fixture

class TokenFixture {
    fun authorization(token: String): String = "Bearer $token"
}
