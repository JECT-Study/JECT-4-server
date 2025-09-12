package com.ject.studytrip.auth.application.dto;

public record OAuthLoginOutcome(Outcome outcome, TokenInfo tokenInfo, String signupKey) {
    enum Outcome {
        SUCCESS,
        SIGNUP_REQUIRED
    }

    public static OAuthLoginOutcome success(
            String accessToken, String refreshToken, long refreshTokenExpiresIn) {
        return new OAuthLoginOutcome(
                Outcome.SUCCESS,
                new TokenInfo(accessToken, refreshToken, refreshTokenExpiresIn),
                null);
    }

    public static OAuthLoginOutcome signupRequired(String signupKey) {
        return new OAuthLoginOutcome(Outcome.SIGNUP_REQUIRED, null, signupKey);
    }

    public boolean isSignupRequired() {
        return this.outcome == Outcome.SIGNUP_REQUIRED;
    }
}
