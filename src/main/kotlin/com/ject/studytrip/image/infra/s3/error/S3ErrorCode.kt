package com.ject.studytrip.image.infra.s3.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class S3ErrorCode(
    private val status: HttpStatus,
    private val message: String,
) : ErrorCode {
    // 502
    S3_STORAGE_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "Storage 서버 에러가 발생했습니다."),
    ;

    override fun getName(): String = name

    override fun getStatus(): HttpStatus = status

    override fun getMessage(): String = message
}
