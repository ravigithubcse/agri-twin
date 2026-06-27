package com.agritwin.farmtwin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FarmTwinResponse(
        UUID id,
        UUID userId,
        Integer version,
        Short profileCompletenessScore,
        List<LandParcelResponse> landParcels,
        Instant createdAt,
        Instant lastUpdated
) {
}
