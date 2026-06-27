package com.agritwin.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        UserResponse user
) {
}
