package com.ject.studytrip.member.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.MEMBER
import com.ject.studytrip.image.application.service.ImageService
import com.ject.studytrip.member.application.dto.MemberDetail
import com.ject.studytrip.member.application.dto.MemberInfo
import com.ject.studytrip.member.application.dto.PresignedProfileImageInfo
import com.ject.studytrip.member.application.service.MemberCommandService
import com.ject.studytrip.member.application.service.MemberQueryService
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest
import com.ject.studytrip.mission.application.service.DailyMissionCommandService
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.studylog.application.service.StudyLogCommandService
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionCommandService
import com.ject.studytrip.studylog.application.service.StudyLogQueryService
import com.ject.studytrip.trip.application.service.DailyGoalCommandService
import com.ject.studytrip.trip.application.service.TripCommandService
import com.ject.studytrip.trip.application.service.TripQueryService
import com.ject.studytrip.trip.application.service.TripReportCommandService
import com.ject.studytrip.trip.application.service.TripReportQueryService
import com.ject.studytrip.trip.application.service.TripReportStudyLogCommandService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MemberFacade(
    // Query Service
    private val memberQueryService: MemberQueryService,
    private val tripQueryService: TripQueryService,
    private val studyLogQueryService: StudyLogQueryService,
    private val tripReportQueryService: TripReportQueryService,
    // Command Service
    private val memberCommandService: MemberCommandService,
    private val tripCommandService: TripCommandService,
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
    private val dailyGoalCommandService: DailyGoalCommandService,
    private val pomodoroCommandService: PomodoroCommandService,
    private val dailyMissionCommandService: DailyMissionCommandService,
    private val studyLogCommandService: StudyLogCommandService,
    private val studyLogDailyMissionCommandService: StudyLogDailyMissionCommandService,
    private val tripReportCommandService: TripReportCommandService,
    private val tripReportStudyLogCommandService: TripReportStudyLogCommandService,
    // Image Service
    private val imageService: ImageService,
) {
    companion object {
        private const val MEMBER_PROFILE_IMAGE_KEY_PREFIX = "members"
    }

    @CacheEvict(
        cacheNames = [MEMBER],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)",
    )
    @Transactional
    fun updateMember(
        memberId: Long,
        request: UpdateMemberRequest,
    ) {
        val member = memberQueryService.getValidMember(memberId)

        memberCommandService.updateMember(member, request)
    }

    @CacheEvict(
        cacheNames = [MEMBER],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)",
    )
    @Transactional
    fun deleteMember(memberId: Long) {
        val member = memberQueryService.getValidMember(memberId)

        memberCommandService.deleteMember(member)
        imageService.cleanup(member.profileImage)
    }

    @Transactional
    fun restoreMember(memberId: Long) {
        val member = memberQueryService.getDeletedMember(memberId)

        memberCommandService.restoreMember(member)
    }

    @Transactional
    fun hardDeleteMemberCascade(memberId: Long) {
        val member = memberQueryService.getValidMember(memberId)

        // 삭제할 이미지 목록
        val imageUrls = collectImageUrlsForMember(member)

        // 멤버와 멤버와 관련된 모든 데이터 즉시 삭제
        cascadeHardDeleteByMemberId(memberId)

        // 이미지 삭제 이벤트 발행, 트랜잭션 커밋 이후 이미지 삭제 처리
        imageService.publishCleanupBatchEvent(imageUrls)
    }

    @Cacheable(
        cacheNames = [MEMBER],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)",
    )
    @Transactional(readOnly = true)
    fun getMemberDetail(memberId: Long): MemberDetail {
        val member = memberQueryService.getValidMember(memberId)
        val tripCount = tripQueryService.getActiveTripCountByMemberId(memberId)
        val studyLogCount = studyLogQueryService.getActiveStudyLogCountByMemberId(memberId)

        val memberInfo = MemberInfo.from(member)

        return MemberDetail.from(memberInfo, tripCount, studyLogCount)
    }

    @Transactional(readOnly = true)
    fun issuePresignedUrl(
        memberId: Long,
        request: PresignProfileImageRequest,
    ): PresignedProfileImageInfo {
        val member = memberQueryService.getValidMember(memberId)

        val info = imageService.presign(MEMBER_PROFILE_IMAGE_KEY_PREFIX, member.id.toString(), request.originFilename)

        return PresignedProfileImageInfo.of(member.id, info.tmpKey, info.presignedUrl)
    }

    @CacheEvict(
        cacheNames = [MEMBER],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).member(#memberId)",
    )
    @Transactional
    fun confirmImage(
        memberId: Long,
        request: ConfirmProfileImageRequest,
    ) {
        val member = memberQueryService.getValidMember(memberId)
        val finalKey = imageService.confirm(request.tmpKey)

        // 기존 이미지 삭제 (이미지가 존재하지 않아도 예외 발생 X)
        imageService.cleanup(member.profileImage)

        // 새로운 이미지 업데이트
        memberCommandService.updateProfileImage(member, finalKey)
    }

    private fun collectImageUrlsForMember(member: Member): List<String> =
        buildList {
            // TripReport 이미지 목록 조회
            addAll(tripReportQueryService.getTripReportImageUrlsByMemberId(member.id))

            // StudyLog 이미지 목록 조회
            addAll(studyLogQueryService.getStudyLogImageUrlsByMemberId(member.id))

            member.profileImage
                ?.takeIf { it.isNotBlank() }
                ?.let { add(it) }
        }

    private fun cascadeHardDeleteByMemberId(memberId: Long) {
        // 자식 -> 부모 순으로 삭제 진행
        tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByMember(memberId)
        tripReportCommandService.hardDeleteTripReportsOwnedByMember(memberId)
        studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByMember(memberId)
        pomodoroCommandService.hardDeletePomodorosOwnedByMember(memberId)
        studyLogCommandService.hardDeleteStudyLogsOwnedByMember(memberId)
        dailyMissionCommandService.hardDeleteDailyMissionsOwnedByMember(memberId)
        dailyGoalCommandService.hardDeleteDailyGoalsOwnedByMember(memberId)
        missionCommandService.hardDeleteMissionsOwnedByMember(memberId)
        stampCommandService.hardDeleteStampsOwnedByMember(memberId)
        tripCommandService.hardDeleteTripsOwnedByMember(memberId)

        memberCommandService.hardDeleteMember(memberId)
    }
}
