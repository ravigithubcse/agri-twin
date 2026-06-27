package com.agritwin.farmtwin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agritwin.jwt")
public record JwtProperties(
        String secret,
        String issuer
) {
}
