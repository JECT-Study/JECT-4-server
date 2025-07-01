package com.ject.studytrip.auth.infra.client;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse;
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.global.exception.error.ErrorCode;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoOauthClient {

    private final WebClient webClient;

    public Mono<KakaoTokenResponse> fetchKakaoTokens(
            String tokenUri, BodyInserters.FormInserter<String> formData) {
        return webClient
                .post()
                .uri(tokenUri)
                .body(formData)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        handleError(AuthErrorCode.KAKAO_TOKEN_FETCH_FAILED))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        handleError(AuthErrorCode.KAKAO_SERVER_ERROR))
                .bodyToMono(KakaoTokenResponse.class);
    }

    public Mono<KakaoUserInfoResponse> fetchKakaoUserInfo(String userInfoUri, String accessToken) {
        return webClient
                .get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        handleError(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        handleError(AuthErrorCode.KAKAO_SERVER_ERROR))
                .bodyToMono(KakaoUserInfoResponse.class);
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleError(ErrorCode errorCode) {
        return response -> Mono.error(new CustomException(errorCode));
    }
}
