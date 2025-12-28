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
import com.ject.studytrip.mission.application.service.DailyMissionCommandService;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.studylog.application.service.StudyLogCommandService;
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionCommandService;
import com.ject.studytrip.studylog.application.service.StudyLogQueryService;
import com.ject.studytrip.trip.application.dto.TripCount;
import com.ject.studytrip.trip.application.service.*;
import java.util.ArrayList;
import java.util.List;
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
    private final TripReportQueryService tripReportQueryService;

    private final MemberCommandService memberCommandService;
    private final TripCommandService tripCommandService;
    private final StampCommandService stampCommandService;
    private final MissionCommandService missionCommandService;
    private final DailyGoalCommandService dailyGoalCommandService;
    private final PomodoroCommandService pomodoroCommandService;
    private final DailyMissionCommandService dailyMissionCommandService;
    private final StudyLogCommandService studyLogCommandService;
    private final StudyLogDailyMissionCommandService studyLogDailyMissionCommandService;
    private final TripReportCommandService tripReportCommandService;
    private final TripReportStudyLogCommandService tripReportStudyLogCommandService;

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
        TripCount tripCount = tripQueryService.getActiveTripCountByMemberId(memberId);
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

    @CacheEvict(
            cacheNames = MEMBER,
            key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)")
    @Transactional
    public void confirmImage(Long memberId, ConfirmProfileImageRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        String finalKey = imageService.confirm(request.tmpKey());

        // 기존 이미지 삭제 (이미지가 존재하지 않아도 예외발생 X)
        imageService.cleanup(member.getProfileImage());

        // 새로운 이미지 업데이트
        memberCommandService.updateProfileImage(member, finalKey);
    }

    @Transactional
    public void hardDeleteMemberCascade(Long memberId) {
        Member member = memberQueryService.getValidMember(memberId);

        // 삭제할 이미지 목록
        List<String> imageUrls = collectImageUrlsForMember(member);

        // 멤버의 모든 데이터 즉시 삭제
        cascadeHardDeleteByMemberId(member.getId());

        // 이미지 삭제 이벤트 발행
        // 트랜잭션 커밋 이후 이미지 삭제 처리
        imageService.publishCleanupBatchEvent(imageUrls);
    }

    @Transactional
    public void restoreMember(Long memberId) {
        Member member = memberQueryService.getDeletedMember(memberId);

        memberCommandService.restoreMember(member);
    }

    private List<String> collectImageUrlsForMember(Member member) {
        List<String> imageUrls = new ArrayList<>();

        // TripReport 이미지 목록 조회
        imageUrls.addAll(tripReportQueryService.getTripReportImageUrlsByMemberId(member.getId()));

        // StudyLog 이미지 목록 조회
        imageUrls.addAll(studyLogQueryService.getStudyLogImageUrlsByMemberId(member.getId()));

        if (member.getProfileImage() != null && !member.getProfileImage().isBlank()) {
            imageUrls.add(member.getProfileImage());
        }
        return imageUrls;
    }

    private void cascadeHardDeleteByMemberId(Long memberId) {
        // 자식 -> 부모 순으로 삭제 진행
        tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsByMember(memberId);
        tripReportCommandService.hardDeleteTripReportsByMember(memberId);

        studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByMember(memberId);
        pomodoroCommandService.hardDeletePomodorosOwnedByMember(memberId);
        studyLogCommandService.hardDeleteStudyLogsOwnedByMember(memberId);
        dailyMissionCommandService.hardDeleteDailyMissionsOwnedByMember(memberId);
        dailyGoalCommandService.hardDeleteDailyGoalsOwnedByMember(memberId);

        missionCommandService.hardDeleteMissionsOwnedByMember(memberId);
        stampCommandService.hardDeleteStampsOwnedByMember(memberId);
        tripCommandService.hardDeleteTripsOwnedByMember(memberId);
        memberCommandService.hardDeleteMemberById(memberId);
    }
}
