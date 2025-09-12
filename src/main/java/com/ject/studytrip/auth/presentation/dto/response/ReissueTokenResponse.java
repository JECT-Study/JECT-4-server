package com.ject.studytrip.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueTokenResponse(@Schema(description = "새로 발급된 엑세스 토큰") String accessToken) {
    public static ReissueTokenResponse of(String accessToken) {
        return new ReissueTokenResponse(accessToken);
    }
}
