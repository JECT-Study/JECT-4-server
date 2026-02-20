package com.ject.studytrip.member.domain.policy

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member

object MemberPolicy {
    fun validateNotDeleted(member: Member) {
        if (member.isDeleted()) {
            throw CustomException(MemberErrorCode.MEMBER_ALREADY_DELETED)
        }
    }

    fun validateDeleted(member: Member) {
        if (!member.isDeleted()) {
            throw CustomException(MemberErrorCode.MEMBER_NOT_DELETED)
        }
    }

    fun validateNotDuplicated(exists: Boolean) {
        if (exists) {
            throw CustomException(MemberErrorCode.MEMBER_ALREADY_EXISTS)
        }
    }
}
