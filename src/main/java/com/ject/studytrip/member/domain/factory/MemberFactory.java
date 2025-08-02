package com.ject.studytrip.member.domain.factory;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.model.SocialProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberFactory {
    public static Member createFromKakao(
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
