package com.ject.studytrip.auth.domain.repository;

public interface RefreshTokenRedisRepository {
    void saveRefreshToken(String memberId, String refreshToken, long refreshTokenExpireTime);

    boolean existsRefreshToken(String refreshToken);

    void deleteRefreshToken(String refreshToken);

    String findMemberIdByRefreshToken(String refreshToken);
}
