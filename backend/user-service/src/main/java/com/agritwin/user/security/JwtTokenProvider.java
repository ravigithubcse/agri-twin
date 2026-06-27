package com.agritwin.user.security;

import com.agritwin.user.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates short-lived JWT access tokens (blueprint 5.5:
 * "JWT (15min access token)"). Refresh tokens are a separate, opaque,
 * server-side-tracked mechanism (see RefreshTokenService) rather than a
 * second JWT — this lets us revoke them server-side, which a stateless JWT
 * cannot do.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String phone, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.accessTokenExpiryMinutes() * 60);

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.issuer())
                .claim("phone", phone)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenExpirySeconds() {
        return properties.accessTokenExpiryMinutes() * 60;
    }

    public long getRefreshTokenExpiryDays() {
        return properties.refreshTokenExpiryDays();
    }

    /**
     * Validates signature and expiry. Returns claims if valid.
     * Throws JwtException (or subclass) if invalid/expired/malformed.
     */
    public Claims validateAndParse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
