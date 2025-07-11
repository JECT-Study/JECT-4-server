package com.ject.studytrip.auth.application.facade;

import com.ject.studytrip.auth.application.service.KakaoLoginService;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.auth.presentation.dto.request.KakaoLoginRequest;
import com.ject.studytrip.auth.presentation.dto.request.KakaoSignupRequest;
import com.ject.studytrip.auth.presentation.dto.response.TokenResponse;
import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final KakaoLoginService kakaoLoginService;
    private final MemberService memberService;

    public TokenResponse kakaoLogin(KakaoLoginRequest request) {
        KakaoUserInfoResponse response = kakaoLoginService.getKakaoUserInfo(request.code());
        Member member =
                memberService.getMemberBySocialProviderAndSocialId(
                        SocialProvider.KAKAO, response.kakaoId());
        return kakaoLoginService.getTokens(member.getId().toString(), member.getRole().name());
    }

    public TokenResponse kakaoSignup(KakaoSignupRequest request) {
        KakaoUserInfoResponse response = kakaoLoginService.getKakaoUserInfo(request.code());
        Member member =
                memberService.createMemberFromKakao(
                        response.kakaoId(),
                        response.getEmail(),
                        response.getProfileImage(),
                        request.category(),
                        request.nickname());
        return kakaoLoginService.getTokens(member.getId().toString(), member.getRole().name());
    }
}
