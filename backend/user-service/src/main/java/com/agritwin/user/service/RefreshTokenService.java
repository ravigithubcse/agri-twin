package com.agritwin.user.service;

import com.agritwin.user.entity.RefreshToken;
import com.agritwin.user.exception.InvalidRefreshTokenException;
import com.agritwin.user.repository.RefreshTokenRepository;
import com.agritwin.user.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Refresh tokens are opaque random strings (NOT JWTs) so that revocation is
 * possible server-side. Only the SHA-256 hash is persisted, mirroring how
 * password hashes are handled, so a database read alone can't be replayed
 * as a valid session.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public String issue(UUID userId, String deviceInfo) {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiryDays() * 24 * 60 * 60))
                .revoked(false)
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public UUID validateAndRotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("token not recognised"));

        if (existing.isRevoked()) {
            throw new InvalidRefreshTokenException("token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("token has expired");
        }

        return existing.getUserId();
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
