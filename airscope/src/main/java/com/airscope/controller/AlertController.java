package com.airscope.controller;

import com.airscope.dto.AlertDto.AlertRequest;
import com.airscope.dto.AlertDto.AlertResponse;
import com.airscope.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AlertController - handles threshold alert configuration.
 *
 * Base URL: /alerts
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Configure threshold alerts for devices")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertController {

    private final AlertService alertService;

    /**
     * POST /alerts
     *
     * Create a new alert for a device.
     *
     * Request body:
     * {
     *   "type": "CO2",
     *   "threshold": 1000.0,
     *   "deviceId": 1
     * }
     *
     * Response (201 Created):
     * { "id": 1, "type": "CO2", "threshold": 1000.0, "deviceId": 1 }
     */
    @PostMapping
    @Operation(summary = "Create a new threshold alert")
    public ResponseEntity<AlertResponse> createAlert(
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AlertResponse response = alertService.createAlert(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /alerts/device/{deviceId}
     *
     * Get all alerts configured for a specific device.
     *
     * Response (200 OK):
     * [
     *   { "id": 1, "type": "CO2",  "threshold": 1000.0, "deviceId": 1 },
     *   { "id": 2, "type": "TEMP", "threshold": 35.0,   "deviceId": 1 }
     * ]
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get all alerts for a device")
    public ResponseEntity<List<AlertResponse>> getDeviceAlerts(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AlertResponse> alerts = alertService.getDeviceAlerts(deviceId, userDetails.getUsername());
        return ResponseEntity.ok(alerts);
    }

    /**
     * DELETE /alerts/{id}
     *
     * Delete a specific alert.
     *
     * Response (204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an alert")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        alertService.deleteAlert(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
