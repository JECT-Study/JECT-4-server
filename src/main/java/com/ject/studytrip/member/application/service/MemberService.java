package com.ject.studytrip.member.application.service;

import static io.jsonwebtoken.lang.Strings.hasText;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.domain.entity.MemberCategory;
import com.ject.studytrip.member.domain.entity.SocialProvider;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
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
        validateNewMember(kakaoId);
        MemberCategory parsedCategory = parseCategory(category);
        validateMemberNickname(nickname);
        Member member =
                MemberFactory.fromKakao(kakaoId, email, profileImage, nickname, parsedCategory);
        return memberRepository.save(member);
    }

    private MemberCategory parseCategory(String category) {
        try {
            return MemberCategory.valueOf(category);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(MemberErrorCode.MEMBER_CATEGORY_REQUIRED);
        }
    }

    private void validateNewMember(String socialId) {
        if (memberRepository
                .findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)
                .isPresent()) {
            throw new CustomException(MemberErrorCode.MEMBER_ALREADY_EXISTS);
        }
    }

    private void validateMemberNickname(String nickname) {
        if (!hasText(nickname)) {
            throw new CustomException(MemberErrorCode.MEMBER_NICKNAME_REQUIRED);
        }
    }
}
