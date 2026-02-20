package com.ject.studytrip.global.util

object EntityExtensions {
    fun Long?.requireId(): Long =
        requireNotNull(this) {
            "엔티티가 아직 저장되지 않아 ID가 존재하지 않습니다."
        }
}
