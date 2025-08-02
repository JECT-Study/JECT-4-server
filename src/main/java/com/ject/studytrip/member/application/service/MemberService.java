package com.ject.studytrip.member.application.service;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.application.dto.CreateMemberCommand;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.factory.MemberFactory;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.policy.MemberPolicy;
import com.ject.studytrip.member.domain.repository.MemberQueryRepository;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    @Transactional
    public Member createMemberFromKakao(CreateMemberCommand command) {
        validateMemberIsUnique(SocialProvider.KAKAO, command.socialId());

        MemberCategory memberCategory = convertToMemberCategory(command.category());
        Member member =
                MemberFactory.createFromKakao(
                        command.socialId(),
                        command.email(),
                        command.profileImage(),
                        command.nickname(),
                        memberCategory);

        return memberRepository.save(member);
    }

    @Transactional
    public void updateNicknameAndCategoryIfPresent(Member member, UpdateMemberRequest request) {
        MemberCategory memberCategory = convertToMemberCategory(request.category());

        member.update(request.nickname(), memberCategory);
    }

    @Transactional
    public void deleteMember(Member member) {
        member.updateDeletedAt();
    }

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

    @Transactional(readOnly = true)
    public Member getActiveMemberById(Long memberId) {
        return memberRepository
                .findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String getRoleByMemberId(String memberId) {
        MemberRole memberRole = memberQueryRepository.findMemberRoleById(Long.valueOf(memberId));

        if (memberRole == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return memberRole.name();
    }

    private void validateMemberIsUnique(SocialProvider socialProvider, String socialId) {
        boolean isMemberDuplicated =
                memberRepository.existsBySocialProviderAndSocialId(socialProvider, socialId);
        MemberPolicy.validateNotDuplicated(isMemberDuplicated);
    }

    private MemberCategory convertToMemberCategory(String categoryName) {
        return hasText(categoryName) ? MemberCategory.from(categoryName) : null;
    }
}
