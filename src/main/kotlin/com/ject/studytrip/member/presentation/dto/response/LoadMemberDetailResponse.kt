package com.ject.studytrip.member.presentation.dto.response

import com.ject.studytrip.member.application.dto.MemberInfo
import com.ject.studytrip.member.domain.model.MemberCategory
import com.ject.studytrip.trip.application.dto.TripCount
import io.swagger.v3.oas.annotations.media.Schema

data class LoadMemberDetailResponse(
    @field:Schema(description = "멤버 ID")
    val memberId: Long,
    @field:Schema(description = "이메일")
    val email: String,
    @field:Schema(description = "닉네임")
    val nickname: String,
    @field:Schema(description = "프로필 이미지")
    val profileImage: String?,
    @field:Schema(description = "멤버 카테고리")
    val category: MemberCategory,
    @field:Schema(description = "코스형 여행 개수")
    val courseTripCount: Long,
    @field:Schema(description = "탐험형 여행 개수")
    val exploreTripCount: Long,
    @field:Schema(description = "학습 기록 개수")
    val studyLogCount: Long,
) {
    companion object {
        fun of(
            memberInfo: MemberInfo,
            tripCount: TripCount,
            studyLogCount: Long,
        ): LoadMemberDetailResponse =
            LoadMemberDetailResponse(
                memberInfo.memberId,
                memberInfo.email,
                memberInfo.nickname,
                memberInfo.profileImage,
                memberInfo.category,
                tripCount.course,
                tripCount.explore,
                studyLogCount,
            )
    }
}
