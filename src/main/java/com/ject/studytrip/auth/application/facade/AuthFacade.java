package com.ject.studytrip.auth.application.facade;

import com.ject.studytrip.auth.application.service.KakaoLoginService;
import com.ject.studytrip.auth.application.service.TokenService;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.request.LogoutRequest;
import com.ject.studytrip.auth.presentation.dto.request.TokenReissueRequest;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
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
    private final TokenService tokenService;
    private final MemberService memberService;

    public TokenResponse kakaoLogin(KakaoLoginRequest request, String origin) {
        KakaoUserInfoResponse response = kakaoLoginService.getKakaoUserInfo(request.code(), origin);

        Member member =
                memberService.getMemberBySocialProviderAndSocialId(
                        SocialProvider.KAKAO, response.kakaoId());

        return tokenService.getTokens(member.getId().toString(), member.getRole().name());
    }

    public TokenResponse kakaoSignup(KakaoSignupRequest request, String origin) {
        KakaoUserInfoResponse response = kakaoLoginService.getKakaoUserInfo(request.code(), origin);
        CreateMemberCommand command =
                CreateMemberCommand.of(
                        response.kakaoId(),
                        response.getEmail(),
                        response.getProfileImage(),
                        request.nickname(),
                        request.category());

        Member member = memberService.createMemberFromKakao(command);

        return tokenService.getTokens(member.getId().toString(), member.getRole().name());
    }

    public TokenResponse reissueToken(TokenReissueRequest request) {
        String memberId = tokenService.getMemberIdByRefreshToken(request.refreshToken());
        String role = memberService.getRoleByMemberId(memberId);

        return tokenService.reissueToken(request.refreshToken(), memberId, role);
    }

    public void logout(LogoutRequest request) {
        tokenService.logout(request.accessToken(), request.refreshToken());
    }
}
