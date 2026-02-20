package com.ject.studytrip.global.exception.response

data class ErrorResponse(
    val error: String,
    val message: String,
    val values: Any?,
) {
    companion object {
        fun of(
            error: String,
            message: String,
            values: Any?,
        ): ErrorResponse = ErrorResponse(error, message, values)

        fun of(
            error: String,
            message: String,
        ): ErrorResponse = ErrorResponse(error, message, null)
    }
}
