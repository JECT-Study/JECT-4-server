package com.ject.studytrip.auth.infra.provider;

import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.global.config.properties.TokenProperties;
import com.ject.studytrip.global.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final TokenProperties tokenProperties;

    public String createAccessToken(String memberId, String role) {
        return createToken(memberId, role, tokenProperties.accessExpirationTime());
    }

    public String createRefreshToken(String memberId, String role) {
        return createToken(memberId, role, tokenProperties.refreshExpirationTime());
    }

    public String extractMemberIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractMemberRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
    }

    private String createToken(String memberId, String role, long expirationSeconds) {
        return Jwts.builder()
                .claims(buildClaims(memberId, role, expirationSeconds))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Map<String, Object> buildClaims(String memberId, String role, long expirationSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);
        return Map.of(
                "sub", memberId, "role", role, "iat", Date.from(now), "exp", Date.from(expiry));
    }

    private Claims parseClaims(String token) {
        try {
            return decodeToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
    }

    private Claims decodeToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(tokenProperties.secret().getBytes());
    }
}
