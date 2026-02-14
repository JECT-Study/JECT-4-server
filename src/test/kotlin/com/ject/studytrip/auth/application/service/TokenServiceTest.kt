package com.ject.studytrip.auth.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository
import com.ject.studytrip.auth.infra.provider.TokenProvider
import com.ject.studytrip.global.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

@DisplayName("TokenService 단위 테스트")
class TokenServiceTest : BaseUnitTest() {
    @InjectMocks
    lateinit var tokenService: TokenService

    @Mock
    lateinit var tokenProvider: TokenProvider

    @Mock
    lateinit var refreshTokenRedisRepository: RefreshTokenRedisRepository

    @Mock
    lateinit var logoutTokenRedisRepository: LogoutTokenRedisRepository

    companion object {
        private const val MEMBER_ID = "123"
        private const val ROLE = "ROLE_USER"
        private const val ACCESS_TOKEN = "access.jwt.token"
        private const val REFRESH_TOKEN = "refresh-token"
        private const val NEW_ACCESS_TOKEN = "newAccess.jwt.token"
        private const val NEW_REFRESH_TOKEN = "newRefresh-token"
        private const val REFRESH_TOKEN_EXPIRATION_TIME = 7200L
        private const val ACCESS_TOKEN_REMAINING_TIME = 300L
    }

    @Nested
    @DisplayName("getTokens 메서드는")
    inner class GetTokens {
        @Test
        @DisplayName("멤버 ID와 Role이 주어지면 엑세스 토큰과 리프레시 토큰을 반환한다.")
        fun shouldReturnTokenResponseWhenMemberIdAndRoleProvided() {
            // given
            `when`(tokenProvider.createAccessToken(MEMBER_ID, ROLE)).thenReturn(ACCESS_TOKEN)
            `when`(tokenProvider.createRefreshToken()).thenReturn(REFRESH_TOKEN)
            `when`(tokenProvider.getRefreshTokenExpirationTime()).thenReturn(REFRESH_TOKEN_EXPIRATION_TIME)

            // when
            val result = tokenService.getTokens(MEMBER_ID, ROLE)

            // then
            assertThat(result.accessToken).isEqualTo(ACCESS_TOKEN)
            assertThat(result.refreshToken).isEqualTo(REFRESH_TOKEN)
            verify(refreshTokenRedisRepository).saveRefreshToken(MEMBER_ID, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_TIME)
        }
    }

    @Nested
    @DisplayName("reissueToken 메서드는")
    inner class ReissueToken {
        @Test
        @DisplayName("유효한 리프레시 토큰이 들어오면, 새로운 엑세스 토큰과 리프레시 토큰을 반환한다.")
        fun shouldReissueTokenWhenRefreshTokenIsValid() {
            // given
            given(tokenProvider.getRefreshTokenExpirationTime()).willReturn(REFRESH_TOKEN_EXPIRATION_TIME)
            given(tokenProvider.createAccessToken(MEMBER_ID, ROLE)).willReturn(NEW_ACCESS_TOKEN)
            given(tokenProvider.createRefreshToken()).willReturn(NEW_REFRESH_TOKEN)

            // when
            val result = tokenService.reissueToken(REFRESH_TOKEN, MEMBER_ID, ROLE)

            // then
            assertThat(result.accessToken).isEqualTo(NEW_ACCESS_TOKEN)
            assertThat(result.refreshToken).isEqualTo(NEW_REFRESH_TOKEN)
            verify(refreshTokenRedisRepository).deleteRefreshToken(REFRESH_TOKEN)
            verify(refreshTokenRedisRepository).saveRefreshToken(MEMBER_ID, NEW_REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_TIME)
        }
    }

    @Nested
    @DisplayName("logout 메서드는")
    inner class Logout {
        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenRefreshTokenDoesNotExistInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(false)

            // when
            val exception = assertThrows<CustomException> { tokenService.logout(ACCESS_TOKEN, REFRESH_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.message)
        }

        @Test
        @DisplayName("유효한 엑세스 토큰과 리프레시 토큰이 들어오면, 엑세스 토큰을 블랙리스트에 저장하고 저장된 리프레시 토큰을 삭제한다.")
        fun shouldLogoutWhenAccessTokenAndRefreshTokenAreValid() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(true)
            given(tokenProvider.getAccessTokenRemainingTime(ACCESS_TOKEN)).willReturn(ACCESS_TOKEN_REMAINING_TIME)

            // when
            tokenService.logout(ACCESS_TOKEN, REFRESH_TOKEN)

            // then
            verify(logoutTokenRedisRepository).saveAccessToken(ACCESS_TOKEN, ACCESS_TOKEN_REMAINING_TIME)
            verify(refreshTokenRedisRepository).deleteRefreshToken(REFRESH_TOKEN)
        }
    }

    @Nested
    @DisplayName("getMemberIdByRefreshToken 메서드는")
    inner class GetMemberIdByRefreshToken {
        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenRefreshTokenDoesNotExistInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(false)

            // when
            val exception = assertThrows<CustomException> { tokenService.getMemberIdByRefreshToken(REFRESH_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.message)
        }

        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하면 멤버 ID를 반환한다.")
        fun shouldReturnMemberIdWhenRefreshTokenExistsInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(true)
            given(refreshTokenRedisRepository.findMemberIdByRefreshToken(REFRESH_TOKEN)).willReturn(MEMBER_ID)

            // when
            val result = tokenService.getMemberIdByRefreshToken(REFRESH_TOKEN)

            // then
            assertThat(result).isEqualTo(MEMBER_ID)
        }
    }

    @Nested
    @DisplayName("setAuthenticationByAccessToken 메서드는")
    inner class SetAuthenticationByAccessToken {
        @Test
        @DisplayName("멤버 ID 추출에 실패하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIdExtractionFails() {
            // given
            `when`(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN)).thenThrow(CustomException(AuthErrorCode.INVALID_JWT_TOKEN))

            // when
            val exception = assertThrows<CustomException> { tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_JWT_TOKEN.message)
        }

        @Test
        @DisplayName("Role 추출에 실패하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenRoleExtractionFails() {
            // given
            `when`(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN)).thenReturn(MEMBER_ID)
            `when`(tokenProvider.extractRoleFromToken(ACCESS_TOKEN)).thenThrow(CustomException(AuthErrorCode.INVALID_JWT_TOKEN))

            // when
            val exception = assertThrows<CustomException> { tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_JWT_TOKEN.message)
        }

        @Test
        @DisplayName("토큰에서 멤버 ID와 Role을 추출하여 SecurityContext에 저장한다.")
        fun shouldSetAuthenticationInSecurityContext() {
            // given
            `when`(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN)).thenReturn(MEMBER_ID)
            `when`(tokenProvider.extractRoleFromToken(ACCESS_TOKEN)).thenReturn(ROLE)

            // when
            tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken::class.java)
            assertThat(authentication.name).isEqualTo(MEMBER_ID)
            assertThat(authentication.authorities.map(GrantedAuthority::getAuthority)).containsExactly(ROLE)
        }
    }

    @Nested
    @DisplayName("validateActiveAccessToken 메서드는")
    inner class ValidateActiveAccessToken {
        @Test
        @DisplayName("유효하지 않은 엑세스 토큰이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenAccessTokenIsInvalid() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(false)

            // when
            val exception = assertThrows<CustomException> { tokenService.validateActiveAccessToken(ACCESS_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.INVALID_JWT_TOKEN.message)
        }

        @Test
        @DisplayName("블랙리스트에 포함된 엑세스 토큰이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenAccessTokenIsBlacklisted() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true)
            given(logoutTokenRedisRepository.existsAccessToken(ACCESS_TOKEN)).willReturn(true)

            // when
            val exception = assertThrows<CustomException> { tokenService.validateActiveAccessToken(ACCESS_TOKEN) }

            // then
            assertThat(exception.message).isEqualTo(AuthErrorCode.TOKEN_IS_BLACKLISTED.message)
        }

        @Test
        @DisplayName("유효하고 블랙리스트에 포함되지 않은 토큰이면 예외가 발생하지 않는다")
        fun shouldPassValidationWhenAccessTokenIsValidAndNotBlacklisted() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true)
            given(logoutTokenRedisRepository.existsAccessToken(ACCESS_TOKEN)).willReturn(false)

            // when & then
            assertDoesNotThrow { tokenService.validateActiveAccessToken(ACCESS_TOKEN) }
        }
    }
}
