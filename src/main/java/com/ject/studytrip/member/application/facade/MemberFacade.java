package com.ject.studytrip.member.application.facade;

import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.application.service.ImageService;
import com.ject.studytrip.member.application.dto.MemberDetail;
import com.ject.studytrip.member.application.dto.MemberInfo;
import com.ject.studytrip.member.application.dto.PresignedProfileImageInfo;
import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.studylog.application.service.StudyLogService;
import com.ject.studytrip.trip.application.dto.TripCount;
import com.ject.studytrip.trip.application.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private static final String MEMBER_PROFILE_IMAGE_KEY_PREFIX = "members";

    private final MemberService memberService;
    private final TripService tripService;
    private final StudyLogService studyLogService;
    private final ImageService imageService;

    public void updateNicknameAndCategoryIfPresent(Long memberId, UpdateMemberRequest request) {
        Member member = memberService.getActiveMemberById(memberId);

        memberService.updateNicknameAndCategoryIfPresent(member, request);
    }

    public void deleteMember(Long memberId) {
        Member member = memberService.getActiveMemberById(memberId);

        memberService.deleteMember(member);
        imageService.cleanup(member.getProfileImage());
    }

    public MemberDetail getMemberDetail(Long memberId) {
        Member member = memberService.getActiveMemberById(memberId);
        TripCount tripCount = tripService.getActiveTripCountsByMemberId(memberId);
        long studyLogCount = studyLogService.getActiveStudyLogCountByMemberId(memberId);

        MemberInfo memberInfo = MemberInfo.from(member);

        return MemberDetail.from(memberInfo, tripCount, studyLogCount);
    }

    @Transactional(readOnly = true)
    public PresignedProfileImageInfo issuePresignedUrl(
            Long memberId, PresignProfileImageRequest request) {
        Member member = memberService.getActiveMemberById(memberId);
        PresignedImageInfo info =
                imageService.presign(
                        MEMBER_PROFILE_IMAGE_KEY_PREFIX,
                        member.getId().toString(),
                        request.originFilename());

        return PresignedProfileImageInfo.of(member.getId(), info.tmpKey(), info.presignedUrl());
    }

    @Transactional
    public void confirmImage(Long memberId, ConfirmProfileImageRequest request) {
        Member member = memberService.getMember(memberId);
        String finalKey = imageService.confirm(request.tmpKey());

        // 기존 이미지 삭제 (이미지가 존재하지 않아도 예외발생 X)
        imageService.cleanup(member.getProfileImage());

        // 새로운 이미지 업데이트
        memberService.updateProfileImage(member, finalKey);
    }
}
