package com.ject.studytrip.member.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.policy.MemberPolicy;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.factory.MemberFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Member getMemberBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId) {
        return memberRepository
                .findBySocialProviderAndSocialId(socialProvider, socialId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NEED_SIGNUP));
    }

    @Transactional
    public Member createMemberFromKakao(
            String kakaoId, String email, String profileImage, String category, String nickname) {
        boolean exists =
                memberRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, kakaoId);
        MemberPolicy.validateNickname(nickname);
        MemberPolicy.validateNewMember(exists);

        MemberCategory memberCategory = MemberCategory.from(category);
        Member member =
                MemberFactory.fromKakao(kakaoId, email, profileImage, nickname, memberCategory);

        return memberRepository.save(member);
    }
}
