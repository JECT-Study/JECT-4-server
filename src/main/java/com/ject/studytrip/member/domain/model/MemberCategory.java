package com.ject.studytrip.member.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;

public enum MemberCategory {
    STUDENT,
    WORKER,
    FREELANCER,
    JOBSEEKER,
    ;

    public static MemberCategory from(String category) {
        if (!hasText(category)) {
            throw new CustomException(MemberErrorCode.MEMBER_CATEGORY_REQUIRED);
        }

        try {
            return MemberCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new CustomException(MemberErrorCode.INVALID_MEMBER_CATEGORY);
        }
    }
}
