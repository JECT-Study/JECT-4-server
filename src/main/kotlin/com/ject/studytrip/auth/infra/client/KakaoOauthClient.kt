package com.ject.studytrip.auth.infra.client

import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.infra.dto.KakaoTokenResponse
import com.ject.studytrip.auth.infra.dto.KakaoUserInfoResponse
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.exception.error.ErrorCode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.function.Function

@Component
class KakaoOauthClient(
    private val webClient: WebClient,
) {
    fun fetchKakaoTokens(
        tokenUri: String,
        formData: BodyInserters.FormInserter<String>,
    ): Mono<KakaoTokenResponse> =
        webClient
            .post()
            .uri(tokenUri)
            .body(formData)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, handleError(AuthErrorCode.KAKAO_TOKEN_FETCH_FAILED))
            .onStatus(HttpStatusCode::is5xxServerError, handleError(AuthErrorCode.KAKAO_SERVER_ERROR))
            .bodyToMono<KakaoTokenResponse>()

    fun fetchKakaoUserInfo(
        userInfoUri: String,
        accessToken: String,
    ): Mono<KakaoUserInfoResponse> =
        webClient
            .get()
            .uri(userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, handleError(AuthErrorCode.KAKAO_USER_INFO_FETCH_FAILED))
            .onStatus(HttpStatusCode::is5xxServerError, handleError(AuthErrorCode.KAKAO_SERVER_ERROR))
            .bodyToMono<KakaoUserInfoResponse>()

    private fun handleError(errorCode: ErrorCode): Function<ClientResponse, Mono<out Throwable>> =
        Function {
            Mono.error(CustomException(errorCode))
        }
}
