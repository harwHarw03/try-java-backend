package com.airscope.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTOs for Device endpoints.
 */
public class DeviceDto {

    /**
     * Used when creating a new device: POST /devices
     */
    @Data
    public static class DeviceRequest {

        @NotBlank(message = "Device name is required")
        private String name;
    }

    /**
     * Used in API responses — safe to expose publicly.
     * Notice: no user password, no internal details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceResponse {

        private Long id;
        private String name;
        private Long userId;  // just the user's ID, not the whole User object
    }
}
