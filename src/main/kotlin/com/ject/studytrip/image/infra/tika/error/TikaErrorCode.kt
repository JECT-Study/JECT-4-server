package com.ject.studytrip.image.infra.tika.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class TikaErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    // 500
    TIKA_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Tika 서버 에러가 발생했습니다."),
}
