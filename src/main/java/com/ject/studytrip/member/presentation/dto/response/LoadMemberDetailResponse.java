package com.ject.studytrip.member.presentation.dto.response;

import com.ject.studytrip.member.application.dto.MemberInfo;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.trip.application.dto.TripCount;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoadMemberDetailResponse(
        @Schema(description = "멤버 ID") Long memberId,
        @Schema(description = "이메일") String email,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "프로필 이미지") String profileImage,
        @Schema(description = "멤버 카테고리") MemberCategory category,
        @Schema(description = "코스형 여행 개수") long courseTripCount,
        @Schema(description = "탐험형 여행 개수") long exploreTripCount,
        @Schema(description = "학습 기록 개수") long studyLogCount) {
    public static LoadMemberDetailResponse of(
            MemberInfo memberInfo, TripCount tripCount, long studyLogCount) {
        return new LoadMemberDetailResponse(
                memberInfo.memberId(),
                memberInfo.email(),
                memberInfo.nickname(),
                memberInfo.profileImage(),
                memberInfo.category(),
                tripCount.getCourse(),
                tripCount.getExplore(),
                studyLogCount);
    }
}
