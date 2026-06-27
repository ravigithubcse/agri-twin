package com.agritwin.farmtwin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LandParcelRequest(

        @NotBlank(message = "Label is required")
        @Size(max = 100)
        String label,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        BigDecimal longitude,

        String soilType,

        String irrigationType,

        @NotNull(message = "Area in acres is required")
        @DecimalMin(value = "0.01", message = "Area must be greater than 0")
        BigDecimal areaAcres,

        String currentCrop,

        LocalDate sowingDate,

        LocalDate expectedHarvestDate
) {
}
