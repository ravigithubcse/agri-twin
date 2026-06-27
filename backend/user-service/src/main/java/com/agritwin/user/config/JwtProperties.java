package com.agritwin.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agritwin.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiryMinutes,
        long refreshTokenExpiryDays,
        String issuer
) {
}
