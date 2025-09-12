package com.ject.studytrip.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.auth.application.dto.TokenInfo;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.domain.repository.LogoutTokenRedisRepository;
import com.ject.studytrip.auth.domain.repository.RefreshTokenRedisRepository;
import com.ject.studytrip.auth.infra.provider.TokenProvider;
import com.ject.studytrip.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("TokenService 단위 테스트")
class TokenServiceTest extends BaseUnitTest {
    private static final String MEMBER_ID = "123";
    private static final String ROLE = "ROLE_USER";
    private static final String ACCESS_TOKEN = "access.jwt.token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String NEW_ACCESS_TOKEN = "newAccess.jwt.token";
    private static final String NEW_REFRESH_TOKEN = "newRefresh-token";
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7200L;
    private static final long ACCESS_TOKEN_REMAINING_TIME = 300L;

    @InjectMocks private TokenService tokenService;

    @Mock private TokenProvider tokenProvider;
    @Mock private RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock private LogoutTokenRedisRepository logoutTokenRedisRepository;

    @Nested
    @DisplayName("getTokens 메서드는")
    class GetTokens {

        @Test
        @DisplayName("멤버 ID와 Role이 주어지면 엑세스 토큰과 리프레시 토큰을 반환한다.")
        void shouldReturnTokenResponseWhenMemberIdAndRoleProvided() {
            // given
            when(tokenProvider.createAccessToken(MEMBER_ID, ROLE)).thenReturn(ACCESS_TOKEN);
            when(tokenProvider.createRefreshToken()).thenReturn(REFRESH_TOKEN);
            when(tokenProvider.getRefreshTokenExpirationTime())
                    .thenReturn(REFRESH_TOKEN_EXPIRATION_TIME);

            // when
            TokenInfo response = tokenService.getTokens(MEMBER_ID, ROLE);

            // then
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            verify(refreshTokenRedisRepository)
                    .saveRefreshToken(MEMBER_ID, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_TIME);
        }
    }

    @Nested
    @DisplayName("reissueToken 메서드는")
    class ReissueToken {

        @Test
        @DisplayName("유효한 리프레시 토큰이 들어오면, 새로운 엑세스 토큰과 리프레시 토큰을 반환한다.")
        void shouldReissueTokenWhenRefreshTokenIsValid() {
            // given
            given(tokenProvider.getRefreshTokenExpirationTime())
                    .willReturn(REFRESH_TOKEN_EXPIRATION_TIME);
            given(tokenProvider.createAccessToken(MEMBER_ID, ROLE)).willReturn(NEW_ACCESS_TOKEN);
            given(tokenProvider.createRefreshToken()).willReturn(NEW_REFRESH_TOKEN);

            // when
            TokenInfo response = tokenService.reissueToken(REFRESH_TOKEN, MEMBER_ID, ROLE);

            // then
            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            verify(refreshTokenRedisRepository).deleteRefreshToken(REFRESH_TOKEN);
            verify(refreshTokenRedisRepository)
                    .saveRefreshToken(MEMBER_ID, NEW_REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION_TIME);
        }
    }

    @Nested
    @DisplayName("logout 메서드는")
    class Logout {

        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenRefreshTokenDoesNotExistInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tokenService.logout(ACCESS_TOKEN, REFRESH_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        @Test
        @DisplayName("유효한 엑세스 토큰과 리프레시 토큰이 들어오면, 엑세스 토큰을 블랙리스트에 저장하고 저장된 리프레시 토큰을 삭제한다.")
        void shouldLogoutWhenAccessTokenAndRefreshTokenAreValid() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(true);
            given(tokenProvider.getAccessTokenRemainingTime(ACCESS_TOKEN))
                    .willReturn(ACCESS_TOKEN_REMAINING_TIME);

            // when
            tokenService.logout(ACCESS_TOKEN, REFRESH_TOKEN);

            // then
            verify(logoutTokenRedisRepository)
                    .saveAccessToken(ACCESS_TOKEN, ACCESS_TOKEN_REMAINING_TIME);
            verify(refreshTokenRedisRepository).deleteRefreshToken(REFRESH_TOKEN);
        }
    }

    @Nested
    @DisplayName("getMemberIdByRefreshToken 메서드는")
    class GetMemberIdByRefreshToken {

        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenRefreshTokenDoesNotExistInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tokenService.getMemberIdByRefreshToken(REFRESH_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        @Test
        @DisplayName("리프레시 토큰이 Redis에 존재하면 멤버 ID를 반환한다.")
        void shouldReturnMemberIdWhenRefreshTokenExistsInRedis() {
            // given
            given(refreshTokenRedisRepository.existsRefreshToken(REFRESH_TOKEN)).willReturn(true);
            given(refreshTokenRedisRepository.findMemberIdByRefreshToken(REFRESH_TOKEN))
                    .willReturn(MEMBER_ID);

            // when
            String result = tokenService.getMemberIdByRefreshToken(REFRESH_TOKEN);

            // then
            assertThat(result).isEqualTo(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("setAuthenticationByAccessToken 메서드는")
    class SetAuthenticationByAccessToken {

        @Test
        @DisplayName("멤버 ID 추출에 실패하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberIdExtractionFails() {
            // given
            when(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN))
                    .thenThrow(new CustomException(AuthErrorCode.INVALID_JWT_TOKEN));

            // when & then
            assertThatThrownBy(() -> tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_JWT_TOKEN.getMessage());
        }

        @Test
        @DisplayName("Role 추출에 실패하면 예외가 발생한다.")
        void shouldThrowExceptionWhenRoleExtractionFails() {
            // given
            when(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN)).thenReturn(MEMBER_ID);
            when(tokenProvider.extractRoleFromToken(ACCESS_TOKEN))
                    .thenThrow(new CustomException(AuthErrorCode.INVALID_JWT_TOKEN));

            // when & then
            assertThatThrownBy(() -> tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_JWT_TOKEN.getMessage());
        }

        @Test
        @DisplayName("토큰에서 멤버 ID와 Role을 추출해 SecurityContext에 저장한다.")
        void shouldSetAuthenticationInSecurityContext() {
            // given
            when(tokenProvider.extractMemberIdFromToken(ACCESS_TOKEN)).thenReturn(MEMBER_ID);
            when(tokenProvider.extractRoleFromToken(ACCESS_TOKEN)).thenReturn(ROLE);

            // when
            tokenService.setAuthenticationByAccessToken(ACCESS_TOKEN);

            // then
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            assertThat(authentication.getName()).isEqualTo(MEMBER_ID);
            assertThat(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                    .containsExactly(ROLE);
        }
    }

    @Nested
    @DisplayName("validateActiveAccessToken 메서드는")
    class ValidateActiveAccessToken {

        @Test
        @DisplayName("유효하지 않은 엑세스 토큰이면 예외가 발생한다.")
        void shouldThrowExceptionWhenAccessTokenIsInvalid() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tokenService.validateActiveAccessToken(ACCESS_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_JWT_TOKEN.getMessage());
        }

        @Test
        @DisplayName("블랙리스트에 포함된 엑세스 토큰이면 예외가 발생한다.")
        void shouldThrowExceptionWhenAccessTokenIsBlacklisted() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(logoutTokenRedisRepository.existsAccessToken(ACCESS_TOKEN)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> tokenService.validateActiveAccessToken(ACCESS_TOKEN))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(AuthErrorCode.TOKEN_IS_BLACKLISTED.getMessage());
        }

        @Test
        @DisplayName("유효하고 블랙리스트에 포함되지 않은 토큰이면 예외가 발생하지 않는다")
        void shouldPassValidationWhenAccessTokenIsValidAndNotBlacklisted() {
            // given
            given(tokenProvider.validateAccessToken(ACCESS_TOKEN)).willReturn(true);
            given(logoutTokenRedisRepository.existsAccessToken(ACCESS_TOKEN)).willReturn(false);

            // when & then
            assertDoesNotThrow(() -> tokenService.validateActiveAccessToken(ACCESS_TOKEN));
        }
    }
}
