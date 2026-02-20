package com.ject.studytrip.global.common.response

import com.ject.studytrip.global.exception.response.ErrorResponse

data class StandardResponse(
    val success: Boolean,
    val status: Int,
    val data: Any?,
) {
    companion object {
        fun success(
            status: Int,
            data: Any?,
        ): StandardResponse = StandardResponse(true, status, data)

        fun fail(
            status: Int,
            errorResponse: ErrorResponse,
        ): StandardResponse = StandardResponse(false, status, errorResponse)
    }
}
