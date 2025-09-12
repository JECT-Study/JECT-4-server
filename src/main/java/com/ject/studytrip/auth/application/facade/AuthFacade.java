package com.ject.studytrip.auth.application.facade;

import com.ject.studytrip.auth.application.dto.OAuthLoginOutcome;
import com.ject.studytrip.auth.application.dto.TokenInfo;
import com.ject.studytrip.auth.application.service.KakaoLoginService;
import com.ject.studytrip.auth.application.service.KakaoSignupProfileService;
import com.ject.studytrip.auth.application.service.TokenService;
import com.ject.studytrip.auth.domain.model.KakaoSignupProfile;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.member.application.dto.CreateMemberCommand;
import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final KakaoLoginService kakaoLoginService;
    private final KakaoSignupProfileService kakaoSignupProfileService;
    private final TokenService tokenService;
    private final MemberService memberService;

    public OAuthLoginOutcome kakaoLogin(KakaoLoginRequest request, String origin) {
        KakaoUserInfoResponse info = kakaoLoginService.getKakaoUserInfo(request.code(), origin);

        return memberService
                .getMemberBySocialProviderAndSocialId(SocialProvider.KAKAO, info.kakaoId())
                // 가입되어 있는 사용자인 경우 토큰 발급
                .map(
                        member ->
                                createLoginOutcomeWithIssuedTokens(
                                        member.getId(), member.getRole().name()))
                // 가입이 필요할 경우 가입 키 발급
                .orElseGet(() -> createSignupRequiredOutcomeWithIssuedSignupKey(info));
    }

    public TokenInfo kakaoSignup(String signupKey, KakaoSignupRequest request) {
        KakaoSignupProfile profile = kakaoSignupProfileService.getSignupProfileByKey(signupKey);
        CreateMemberCommand command =
                CreateMemberCommand.of(
                        profile.socialId(),
                        profile.email(),
                        profile.profileImageUrl(),
                        request.nickname(),
                        request.category());

        Member member = memberService.createMemberFromKakao(command);

        kakaoSignupProfileService.deleteBySignupKey(signupKey);

        return tokenService.getTokens(member.getId().toString(), member.getRole().name());
    }

    public TokenInfo reissueToken(String refreshToken) {
        String memberId = tokenService.getMemberIdByRefreshToken(refreshToken);
        String role = memberService.getRoleByMemberId(memberId);

        return tokenService.reissueToken(refreshToken, memberId, role);
    }

    public void logout(LogoutRequest request, String refreshToken) {
        tokenService.logout(request.accessToken(), refreshToken);
    }

    private OAuthLoginOutcome createLoginOutcomeWithIssuedTokens(Long memberId, String roleName) {
        // 토큰 발급
        TokenInfo tokens = tokenService.getTokens(memberId.toString(), roleName);
        return OAuthLoginOutcome.success(
                tokens.accessToken(), tokens.refreshToken(), tokens.refreshTokenExpiresIn());
    }

    private OAuthLoginOutcome createSignupRequiredOutcomeWithIssuedSignupKey(
            KakaoUserInfoResponse info) {
        // 카카오 가입 프로필 임시 저장 및 키 발급
        String signupKey =
                kakaoSignupProfileService.saveAndIssueSignupKey(
                        info.kakaoId(), info.getEmail(), info.getProfileImage());
        return OAuthLoginOutcome.signupRequired(signupKey);
    }
}
