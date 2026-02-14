package com.ject.studytrip.auth.infra.provider

import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.global.config.properties.TokenProperties
import com.ject.studytrip.global.exception.CustomException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class TokenProvider(
    private val tokenProperties: TokenProperties,
) {
    fun createAccessToken(
        memberId: String,
        role: String,
    ): String = createToken(memberId, role, tokenProperties.accessExpirationTime)

    fun createRefreshToken(): String = UUID.randomUUID().toString()

    fun extractMemberIdFromToken(token: String): String = parseClaims(token).subject

    fun extractRoleFromToken(token: String): String = parseClaims(token)["role"] as String

    fun validateAccessToken(accessToken: String): Boolean = runCatching { parseClaims(accessToken) }.isSuccess

    fun getAccessTokenRemainingTime(accessToken: String): Long {
        val expirationTime = parseClaims(accessToken).expiration.time
        val remaining = expirationTime - System.currentTimeMillis()

        return maxOf(remaining, 0)
    }

    fun getRefreshTokenExpirationTime(): Long = tokenProperties.refreshExpirationTime

    private fun createToken(
        memberId: String,
        role: String,
        expirationSeconds: Long,
    ): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(expirationSeconds)

        return Jwts
            .builder()
            .subject(memberId)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseClaims(token: String): Claims =
        runCatching {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        }.getOrElse {
            throw CustomException(AuthErrorCode.INVALID_JWT_TOKEN)
        }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(tokenProperties.secret.toByteArray())
    }
}
