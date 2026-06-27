package com.agritwin.farmtwin.dto;

import com.agritwin.farmtwin.entity.Season;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropHistoryRequest(

        @NotBlank(message = "Crop name is required")
        String cropName,

        @NotNull(message = "Season is required")
        Season season,

        @DecimalMin(value = "0.0", message = "Yield cannot be negative")
        BigDecimal yieldQuintals,

        @DecimalMin(value = "0.0", message = "Income cannot be negative")
        BigDecimal incomeInr,

        @DecimalMin(value = "0.0", message = "Input cost cannot be negative")
        BigDecimal inputCostInr,

        String marketName,

        LocalDate saleDate
) {
}
