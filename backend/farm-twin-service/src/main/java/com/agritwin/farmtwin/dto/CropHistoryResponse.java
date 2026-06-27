package com.agritwin.farmtwin.dto;

import com.agritwin.farmtwin.entity.Season;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CropHistoryResponse(
        UUID id,
        String cropName,
        Season season,
        BigDecimal yieldQuintals,
        BigDecimal incomeInr,
        BigDecimal inputCostInr,
        String marketName,
        LocalDate saleDate,
        Instant createdAt
) {
}
