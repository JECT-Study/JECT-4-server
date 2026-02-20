package com.ject.studytrip.auth.application.facade

import com.ject.studytrip.auth.application.dto.OAuthLoginOutcome
import com.ject.studytrip.auth.application.dto.TokenInfo
import com.ject.studytrip.auth.application.service.KakaoLoginService
import com.ject.studytrip.auth.application.service.KakaoSignupProfileService
import com.ject.studytrip.auth.application.service.TokenService
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.application.dto.CreateMemberCommand
import com.ject.studytrip.member.application.service.MemberCommandService
import com.ject.studytrip.member.application.service.MemberQueryService
import com.ject.studytrip.member.domain.model.SocialProvider
import org.springframework.stereotype.Component

@Component
class AuthFacade(
    private val kakaoSignupProfileService: KakaoSignupProfileService,
    private val kakaoLoginService: KakaoLoginService,
    private val tokenService: TokenService,
    private val memberQueryService: MemberQueryService,
    private val memberCommandService: MemberCommandService,
) {
    fun kakaoSignup(
        signupKey: String?,
        request: KakaoSignupRequest,
    ): TokenInfo {
        val profile = kakaoSignupProfileService.getSignupProfileByKey(signupKey)
        val command = CreateMemberCommand(profile.socialId, profile.email, profile.profileImageUrl, request.nickname, request.category)

        val member = memberCommandService.createMemberFromKakao(command)
        kakaoSignupProfileService.deleteBySignupKey(signupKey)

        return tokenService.getTokens(member.id.toString(), member.role.name)
    }

    fun kakaoLogin(
        request: KakaoLoginRequest,
        origin: String,
    ): OAuthLoginOutcome {
        val info = kakaoLoginService.getKakaoUserInfo(request.code, origin)

        return memberQueryService
            .getMemberBySocialProviderAndSocialId(SocialProvider.KAKAO, info.kakaoId)
            .orElse(null)
            ?.let { member -> createLoginOutcomeWithIssuedTokens(member.id.requireId(), member.role.name) } // 가입되어 있는 사용자인 경우 토큰 발급
            ?: createSignupRequiredOutcomeWithIssuedSignupKey(info) // 가입이 필요할 경우 가입 키 발급
    }

    fun reissueToken(refreshToken: String?): TokenInfo {
        val memberId = tokenService.getMemberIdByRefreshToken(refreshToken)
        val memberRole = memberQueryService.getMemberRoleByMemberId(memberId.toLong())

        return tokenService.reissueToken(refreshToken, memberId, memberRole.name)
    }

    fun logout(
        request: LogoutRequest,
        refreshToken: String?,
    ) = tokenService.logout(request.accessToken, refreshToken)

    private fun createLoginOutcomeWithIssuedTokens(
        memberId: Long,
        roleName: String,
    ): OAuthLoginOutcome {
        // 토큰 발급
        val tokens = tokenService.getTokens(memberId.toString(), roleName)

        return OAuthLoginOutcome.success(tokens.accessToken, tokens.refreshToken, tokens.refreshTokenExpiresIn)
    }

    private fun createSignupRequiredOutcomeWithIssuedSignupKey(info: KakaoUserInfoResponse): OAuthLoginOutcome {
        // 카카오 가입 프로필 임시 저장 및 키 발급
        val signupKey = kakaoSignupProfileService.saveAndIssueSignupKey(info.kakaoId, info.email, info.profileImage)

        return OAuthLoginOutcome.signupRequired(signupKey)
    }
}
