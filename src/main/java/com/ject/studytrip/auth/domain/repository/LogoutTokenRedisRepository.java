package com.ject.studytrip.auth.domain.repository;

public interface LogoutTokenRedisRepository {
    void saveAccessToken(String accessToken, long accessTokenExpirationTime);

    boolean existsAccessToken(String accessToken);
}
