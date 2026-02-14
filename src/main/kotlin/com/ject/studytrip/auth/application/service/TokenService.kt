package com.ject.studytrip.auth.application.service

import com.ject.studytrip.auth.application.dto.TokenInfo
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository
import com.ject.studytrip.auth.infra.provider.TokenProvider
import com.ject.studytrip.global.exception.CustomException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val tokenProvider: TokenProvider,
    private val logoutTokenRedisRepository: LogoutTokenRedisRepository,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
) {
    fun getTokens(
        memberId: String,
        role: String,
    ): TokenInfo {
        val accessToken = tokenProvider.createAccessToken(memberId, role)
        val refreshToken = tokenProvider.createRefreshToken()
        val refreshTokenExpirationTime = tokenProvider.getRefreshTokenExpirationTime()

        refreshTokenRedisRepository.saveRefreshToken(memberId, refreshToken, refreshTokenExpirationTime)

        return TokenInfo(accessToken, refreshToken, refreshTokenExpirationTime)
    }

    fun reissueToken(
        refreshToken: String?,
        memberId: String,
        role: String,
    ): TokenInfo {
        val newAccessToken = tokenProvider.createAccessToken(memberId, role)
        val newRefreshToken = tokenProvider.createRefreshToken()
        val refreshTokenExpirationTime = tokenProvider.getRefreshTokenExpirationTime()

        refreshTokenRedisRepository.deleteRefreshToken(refreshToken)
        refreshTokenRedisRepository.saveRefreshToken(memberId, newRefreshToken, refreshTokenExpirationTime)

        return TokenInfo(newAccessToken, newRefreshToken, refreshTokenExpirationTime)
    }

    fun logout(
        accessToken: String,
        refreshToken: String?,
    ) {
        validateRefreshToken(refreshToken)

        val accessTokenRemainingTime = tokenProvider.getAccessTokenRemainingTime(accessToken)

        logoutTokenRedisRepository.saveAccessToken(accessToken, accessTokenRemainingTime)
        refreshTokenRedisRepository.deleteRefreshToken(refreshToken)
    }

    fun getMemberIdByRefreshToken(refreshToken: String?): String {
        validateRefreshToken(refreshToken)

        return refreshTokenRedisRepository
            .findMemberIdByRefreshToken(refreshToken)
            ?: throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
    }

    fun setAuthenticationByAccessToken(accessToken: String) {
        val memberId = tokenProvider.extractMemberIdFromToken(accessToken)
        val role = tokenProvider.extractRoleFromToken(accessToken)
        val authorities = listOf(SimpleGrantedAuthority(role))
        val authentication = UsernamePasswordAuthenticationToken(memberId, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }

    fun validateActiveAccessToken(accessToken: String) {
        if (!tokenProvider.validateAccessToken(accessToken)) {
            throw CustomException(AuthErrorCode.INVALID_JWT_TOKEN)
        }

        if (logoutTokenRedisRepository.existsAccessToken(accessToken)) {
            throw CustomException(AuthErrorCode.TOKEN_IS_BLACKLISTED)
        }
    }

    private fun validateRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) {
            throw CustomException(AuthErrorCode.MISSING_REFRESH_TOKEN)
        }

        if (!refreshTokenRedisRepository.existsRefreshToken(refreshToken)) {
            throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }
    }
}
