package com.ject.studytrip.member.domain.model

enum class MemberCategory {
    STUDENT,
    WORKER,
    FREELANCER,
    JOBSEEKER,
    ;

    companion object {
        fun from(category: String): MemberCategory = valueOf(category)
    }
}
