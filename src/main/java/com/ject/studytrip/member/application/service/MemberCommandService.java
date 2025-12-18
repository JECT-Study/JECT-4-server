package com.ject.studytrip.member.application.service;

import static io.jsonwebtoken.lang.Strings.hasText;

import com.ject.studytrip.member.application.dto.CreateMemberCommand;
import com.ject.studytrip.member.domain.factory.MemberFactory;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.policy.MemberPolicy;
import com.ject.studytrip.member.domain.repository.MemberCommandRepository;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;
    private final MemberCommandRepository memberCommandRepository;

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

    public void updateNicknameAndCategoryIfPresent(Member member, UpdateMemberRequest request) {
        MemberCategory memberCategory = convertToMemberCategory(request.category());

        member.update(request.nickname(), memberCategory);
    }

    public void updateProfileImage(Member member, String profileImage) {
        MemberPolicy.validateNotDeleted(member);

        member.updateProfileImage(profileImage);
    }

    public void deleteMember(Member member) {
        member.updateDeletedAt();
    }

    public void restoreMember(Member member) {
        member.restoreDeletedAt();
    }

    public long hardDeleteMembers() {
        return memberCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public void hardDeleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
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
