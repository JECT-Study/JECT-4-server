package com.ject.studytrip.global.exception.response

data class FieldErrorResponse(
    val field: String,
    val reason: String,
) {
    companion object {
        fun of(
            field: String,
            reason: String,
        ): FieldErrorResponse = FieldErrorResponse(field, reason)
    }
}
