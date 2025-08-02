package com.ject.studytrip.global.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheKeyConstants {
    AUTH_REISSUE_TOKEN_PREFIX("auth::reissue::token:"),
    AUTH_LOGOUT_TOKEN_PREFIX("auth::logout::token:");

    private final String value;
}
