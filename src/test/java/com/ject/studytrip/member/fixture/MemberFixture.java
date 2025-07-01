package com.ject.studytrip.member.fixture;

import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.domain.entity.MemberCategory;
import com.ject.studytrip.member.domain.entity.MemberRole;
import com.ject.studytrip.member.domain.entity.SocialProvider;

public class MemberFixture {
    private static final String NICKNAME = "민우";
    private static final MemberCategory MEMBER_CATEGORY = MemberCategory.STUDENT;

    public static Member createMemberFromKakao() {
        return Member.of(
                SocialProvider.KAKAO,
                "12345",
                "choi@kakao.com",
                NICKNAME,
                "https://kakao.com/profile.jpg",
                MEMBER_CATEGORY,
                MemberRole.ROLE_USER);
    }

    public static Member createMemberWithoutProfileImageFromKakao() {
        return Member.of(
                SocialProvider.KAKAO,
                "12345",
                "choi@kakao.com",
                NICKNAME,
                null,
                MEMBER_CATEGORY,
                MemberRole.ROLE_USER);
    }
}
