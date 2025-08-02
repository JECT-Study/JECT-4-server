package com.ject.studytrip.auth.application.service;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository;
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository;
import com.ject.studytrip.auth.infra.provider.TokenProvider;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
import com.ject.studytrip.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenProvider tokenProvider;
    private final LogoutTokenRedisRepository logoutTokenRedisRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public TokenResponse getTokens(String memberId, String role) {
        String accessToken = tokenProvider.createAccessToken(memberId, role);
        String refreshToken = tokenProvider.createRefreshToken();
        long refreshTokenExpirationTime = tokenProvider.getRefreshTokenExpirationTime();

        refreshTokenRedisRepository.saveRefreshToken(
                memberId, refreshToken, refreshTokenExpirationTime);

        return TokenResponse.of(accessToken, refreshToken);
    }

    public TokenResponse reissueToken(String refreshToken, String memberId, String role) {
        long refreshTokenExpirationTime = tokenProvider.getRefreshTokenExpirationTime();
        String newAccessToken = tokenProvider.createAccessToken(memberId, role);
        String newRefreshToken = tokenProvider.createRefreshToken();

        refreshTokenRedisRepository.deleteRefreshToken(refreshToken);
        refreshTokenRedisRepository.saveRefreshToken(
                memberId, newRefreshToken, refreshTokenExpirationTime);

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    public void logout(String accessToken, String refreshToken) {
        validateRefreshToken(refreshToken);

        long accessTokenRemainingTime = tokenProvider.getAccessTokenRemainingTime(accessToken);

        logoutTokenRedisRepository.saveAccessToken(accessToken, accessTokenRemainingTime);
        refreshTokenRedisRepository.deleteRefreshToken(refreshToken);
    }

    public String getMemberIdByRefreshToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        return refreshTokenRedisRepository.findMemberIdByRefreshToken(refreshToken);
    }

    public void setAuthenticationByAccessToken(String accessToken) {
        String memberId = tokenProvider.extractMemberIdFromToken(accessToken);
        String role = tokenProvider.extractRoleFromToken(accessToken);
        var authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void validateActiveAccessToken(String accessToken) {
        if (!tokenProvider.validateAccessToken(accessToken)) {
            throw new CustomException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
        if (logoutTokenRedisRepository.existsAccessToken(accessToken)) {
            throw new CustomException(AuthErrorCode.TOKEN_IS_BLACKLISTED);
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (!refreshTokenRedisRepository.existsRefreshToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
