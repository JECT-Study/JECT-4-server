package com.ject.studytrip.member.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.*;

import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.application.service.ImageService;
import com.ject.studytrip.member.application.dto.MemberDetail;
import com.ject.studytrip.member.application.dto.MemberInfo;
import com.ject.studytrip.member.application.dto.PresignedProfileImageInfo;
import com.ject.studytrip.member.application.service.MemberCommandService;
import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.studylog.application.service.StudyLogQueryService;
import com.ject.studytrip.trip.application.dto.TripCountInfo;
import com.ject.studytrip.trip.application.service.TripQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private static final String MEMBER_PROFILE_IMAGE_KEY_PREFIX = "members";

    private final MemberQueryService memberQueryService;
    private final TripQueryService tripQueryService;
    private final StudyLogQueryService studyLogQueryService;

    private final MemberCommandService memberCommandService;

    private final ImageService imageService;

    @CacheEvict(
            cacheNames = MEMBER,
            key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)")
    @Transactional
    public void updateNicknameAndCategoryIfPresent(Long memberId, UpdateMemberRequest request) {
        Member member = memberQueryService.getValidMember(memberId);

        memberCommandService.updateNicknameAndCategoryIfPresent(member, request);
    }

    @CacheEvict(
            cacheNames = MEMBER,
            key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)")
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberQueryService.getValidMember(memberId);

        memberCommandService.deleteMember(member);
        imageService.cleanup(member.getProfileImage());
    }

    @Cacheable(
            cacheNames = MEMBER,
            key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)")
    @Transactional(readOnly = true)
    public MemberDetail getMemberDetail(Long memberId) {
        Member member = memberQueryService.getValidMember(memberId);
        TripCountInfo tripCount = tripQueryService.getActiveTripCountsByMemberId(memberId);
        long studyLogCount = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId);

        MemberInfo memberInfo = MemberInfo.from(member);

        return MemberDetail.from(memberInfo, tripCount, studyLogCount);
    }

    @Transactional(readOnly = true)
    public PresignedProfileImageInfo issuePresignedUrl(
            Long memberId, PresignProfileImageRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        PresignedImageInfo info =
                imageService.presign(
                        MEMBER_PROFILE_IMAGE_KEY_PREFIX,
                        member.getId().toString(),
                        request.originFilename());

        return PresignedProfileImageInfo.of(member.getId(), info.tmpKey(), info.presignedUrl());
    }

    @Transactional
    public void confirmImage(Long memberId, ConfirmProfileImageRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        String finalKey = imageService.confirm(request.tmpKey());

        // 기존 이미지 삭제 (이미지가 존재하지 않아도 예외발생 X)
        imageService.cleanup(member.getProfileImage());

        // 새로운 이미지 업데이트
        memberCommandService.updateProfileImage(member, finalKey);
    }
}
