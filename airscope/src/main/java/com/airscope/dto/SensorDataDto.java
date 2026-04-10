package com.airscope.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTOs for Sensor Data endpoints.
 */
public class SensorDataDto {

    /**
     * Used when a device sends sensor data: POST /data
     */
    @Data
    public static class SensorDataRequest {

        @NotBlank(message = "Device ID is required")
        private String deviceId;

        @NotNull(message = "Temperature is required")
        @Min(value = -50, message = "Temperature must be at least -50°C")
        @Max(value = 60, message = "Temperature must be at most 60°C")
        private Double temperature;

        @NotNull(message = "Humidity is required")
        @Min(value = 0, message = "Humidity must be at least 0%")
        @Max(value = 100, message = "Humidity must be at most 100%")
        private Double humidity;

        @NotNull(message = "CO2 is required")
        @Min(value = 0, message = "CO2 must be at least 0 ppm")
        @Max(value = 10000, message = "CO2 must be at most 10000 ppm")
        private Double co2;

        @NotNull(message = "PM2.5 is required")
        @Min(value = 0, message = "PM2.5 must be at least 0 µg/m³")
        @Max(value = 1000, message = "PM2.5 must be at most 1000 µg/m³")
        private Double pm25;

        private String timestamp;
    }

    /**
     * Used in API responses for sensor readings.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorDataResponse {

        private String deviceId;
        private String timestamp;
        private Double temperature;
        private Double humidity;
        private Double co2;
        private Double pm25;
    }

    /**
     * Response for analytics: air quality score
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AirQualityScoreResponse {

        private String deviceId;
        private Double score;        // 0-100 score
        private String category;     // "Good", "Moderate", "Poor", "Hazardous"
        private String explanation;  // human-readable breakdown
    }

    /**
     * Response for trend analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendResponse {

        private String deviceId;
        private String metric;       // e.g., "CO2"
        private String trend;        // "INCREASING", "DECREASING", "STABLE"
        private Double averageValue;
        private Double latestValue;
        private String message;
    }

    /**
     * Paginated response wrapper
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedSensorDataResponse {

        private List<SensorDataResponse> data;
        private String deviceId;
        private int page;
        private int size;
        private boolean hasMore;
    }
}
