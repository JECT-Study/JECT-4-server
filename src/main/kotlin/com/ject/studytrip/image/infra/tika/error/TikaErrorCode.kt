package com.ject.studytrip.image.infra.tika.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class TikaErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 500
    TIKA_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Tika 서버 에러가 발생했습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
