package com.ject.studytrip.member.application.dto;

import com.ject.studytrip.global.util.DateUtil;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.model.SocialProvider;

public record MemberInfo(
        Long memberId,
        SocialProvider socialProvider,
        String socialId,
        String email,
        String nickname,
        String profileImage,
        MemberCategory category,
        MemberRole role,
        String createdAt,
        String updatedAt,
        String deletedAt) {
    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getSocialProvider(),
                member.getSocialId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImage(),
                member.getCategory(),
                member.getRole(),
                DateUtil.formatDateTime(member.getCreatedAt()),
                DateUtil.formatDateTime(member.getUpdatedAt()),
                DateUtil.formatDateTime(member.getDeletedAt()));
    }
}
