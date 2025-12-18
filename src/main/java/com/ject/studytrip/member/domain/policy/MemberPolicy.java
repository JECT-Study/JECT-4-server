package com.ject.studytrip.member.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberPolicy {
    public static void validateNotDuplicated(boolean exists) {
        if (exists) {
            throw new CustomException(MemberErrorCode.MEMBER_ALREADY_EXISTS);
        }
    }

    public static void validateNotDeleted(Member member) {
        if (member.getDeletedAt() != null) {
            throw new CustomException(MemberErrorCode.MEMBER_ALREADY_DELETED);
        }
    }

    public static void validateDeleted(Member member) {
        if (member.getDeletedAt() == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_DELETED);
        }
    }
}
