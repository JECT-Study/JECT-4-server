package com.ject.studytrip.member.domain.policy;

import static io.jsonwebtoken.lang.Strings.hasText;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberPolicy {

    public static void validateNickname(String nickname) {
        if (!hasText(nickname)) {
            throw new CustomException(MemberErrorCode.MEMBER_NICKNAME_REQUIRED);
        }
    }

    public static void validateNewMember(boolean exists) {
        if (exists) {
            throw new CustomException(MemberErrorCode.MEMBER_ALREADY_EXISTS);
        }
    }
}
