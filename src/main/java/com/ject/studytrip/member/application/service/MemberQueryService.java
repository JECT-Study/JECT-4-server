package com.ject.studytrip.member.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.policy.MemberPolicy;
import com.ject.studytrip.member.domain.repository.MemberQueryRepository;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryService {
    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    public Optional<Member> getMemberBySocialProviderAndSocialId(
            SocialProvider socialProvider, String socialId) {
        return memberRepository
                .findBySocialProviderAndSocialId(socialProvider, socialId)
                .map(
                        member -> {
                            MemberPolicy.validateNotDeleted(member);
                            return member;
                        });
    }

    public Member getMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getValidMember(Long memberId) {
        return memberRepository
                .findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public String getRoleByMemberId(String memberId) {
        MemberRole memberRole =
                memberQueryRepository
                        .findMemberRoleById(Long.valueOf(memberId))
                        .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        return memberRole.name();
    }
}
