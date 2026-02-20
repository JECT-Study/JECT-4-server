package com.ject.studytrip.image.domain.error

import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class ImageErrorCode(
    override val status: HttpStatus,
    override val message: String,
) : ErrorCode {
    // 400
    INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 확장자 입니다."),
    INVALID_IMAGE_MIME(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 MIME 입니다."),
    INVALID_IMAGE_KEY_PREFIX(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 키 PREFIX 입니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 키 입니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 파일 크기가 허용된 최대 크기(5MB)를 초과했습니다."),
    EMPTY_IMAGE(HttpStatus.BAD_REQUEST, "이미지 파일이 비어 있습니다."),
}
