package com.agritwin.farmtwin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LandParcelResponse(
        UUID id,
        String label,
        BigDecimal latitude,
        BigDecimal longitude,
        String soilType,
        String irrigationType,
        BigDecimal areaAcres,
        String currentCrop,
        LocalDate sowingDate,
        LocalDate expectedHarvestDate,
        List<CropHistoryResponse> cropHistory,
        Instant createdAt,
        Instant updatedAt
) {
}
