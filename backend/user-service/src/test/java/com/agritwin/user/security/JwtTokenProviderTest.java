package com.agritwin.user.security;

import com.agritwin.user.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private final JwtProperties properties = new JwtProperties(
            "test-secret-key-for-unit-tests-must-be-long-enough-for-hs256-xxxxxxxxxxx",
            15L, 30L, "agritwin-ai-test");

    private final JwtTokenProvider provider = new JwtTokenProvider(properties);

    @Test
    @DisplayName("generates a token that can be validated and parsed back")
    void generateAndValidate_roundTrips() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "9876543210", "FARMER");

        Claims claims = provider.validateAndParse(token);

        assertThat(provider.getUserId(claims)).isEqualTo(userId);
        assertThat(provider.getRole(claims)).isEqualTo("FARMER");
        assertThat(claims.get("phone", String.class)).isEqualTo("9876543210");
        assertThat(claims.getIssuer()).isEqualTo("agritwin-ai-test");
    }

    @Test
    @DisplayName("rejects a token signed with a different key")
    void validate_rejects_tokenSignedWithDifferentKey() {
        JwtProperties otherProperties = new JwtProperties(
                "a-completely-different-secret-key-also-long-enough-for-hs256-yyyyyyyy",
                15L, 30L, "agritwin-ai-test");
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProperties);

        String token = otherProvider.generateAccessToken(UUID.randomUUID(), "9876543210", "FARMER");

        assertThatThrownBy(() -> provider.validateAndParse(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("rejects an already-expired token")
    void validate_rejects_expiredToken() {
        JwtProperties shortLivedProperties = new JwtProperties(properties.secret(), 0L, 30L, "agritwin-ai-test");
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProperties);

        String token = shortLivedProvider.generateAccessToken(UUID.randomUUID(), "9876543210", "FARMER");

        assertThatThrownBy(() -> shortLivedProvider.validateAndParse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("returns configured access token expiry in seconds")
    void getAccessTokenExpirySeconds_returnsConfiguredValue() {
        assertThat(provider.getAccessTokenExpirySeconds()).isEqualTo(15 * 60);
    }
}
