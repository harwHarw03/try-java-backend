package com.airscope.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTOs for Alert endpoints.
 */
public class AlertDto {

    /**
     * Used when creating a new alert.
     */
    @Data
    public static class AlertRequest {

        @NotBlank(message = "Alert type is required (CO2, TEMP, PM25, HUMIDITY)")
        @Pattern(regexp = "^(CO2|TEMP|PM25|HUMIDITY)$", 
                 flags = Pattern.Flag.CASE_INSENSITIVE,
                 message = "Alert type must be one of: CO2, TEMP, PM25, HUMIDITY")
        private String type;

        @NotNull(message = "Threshold value is required")
        @DecimalMin(value = "0.0", message = "Threshold must be at least 0")
        private Double threshold;

        @NotNull(message = "Device ID is required")
        private Long deviceId;
    }

    /**
     * Used in API responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertResponse {

        private Long id;
        private String type;
        private Double threshold;
        private Long deviceId;
    }
}
