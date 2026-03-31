package com.airscope.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
        private Double temperature;

        @NotNull(message = "Humidity is required")
        private Double humidity;

        @NotNull(message = "CO2 is required")
        private Double co2;

        @NotNull(message = "PM2.5 is required")
        private Double pm25;

        // Optional: client can provide a timestamp, otherwise server generates one
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
}
