package com.ject.studytrip.global.exception.error

import org.springframework.http.HttpStatus

interface ErrorCode {
    val name: String
    val status: HttpStatus
    val message: String
}
