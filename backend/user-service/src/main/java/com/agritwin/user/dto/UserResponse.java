package com.agritwin.user.dto;

import com.agritwin.user.entity.SubscriptionTier;
import com.agritwin.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String phone,
        String fullName,
        UserRole role,
        SubscriptionTier tier,
        String stateCode,
        String districtCode,
        String languagePreference,
        Instant createdAt
) {
}
