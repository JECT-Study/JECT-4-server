package com.ject.studytrip.member.factory;

import com.ject.studytrip.member.domain.entity.Member;
import com.ject.studytrip.member.domain.entity.MemberCategory;
import com.ject.studytrip.member.domain.entity.MemberRole;
import com.ject.studytrip.member.domain.entity.SocialProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberFactory {
    public static Member fromKakao(
            String kakaoId,
            String email,
            String profileImage,
            String nickname,
            MemberCategory category) {
        return Member.of(
                SocialProvider.KAKAO,
                kakaoId,
                email,
                nickname,
                profileImage,
                category,
                MemberRole.ROLE_USER);
    }
}
