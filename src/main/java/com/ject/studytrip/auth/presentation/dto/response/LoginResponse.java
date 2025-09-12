package com.ject.studytrip.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        @Schema(description = "회원가입 필요 여부") boolean signupRequired,
        @Schema(description = "엑세스 토큰(가입된 회원인 경우)") String accessToken) {
    public static LoginResponse success(String accessToken) {
        return new LoginResponse(false, accessToken);
    }

    public static LoginResponse requiredSignup() {
        return new LoginResponse(true, null);
    }
}
