package com.airscope.controller;

import com.airscope.dto.SensorDataDto.*;
import com.airscope.service.SensorDataService;
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
 * SensorDataController - handles sensor data ingestion and analytics.
 *
 * Base URL: /data and /devices/{id}/...
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Sensor Data", description = "Submit and query IoT sensor readings")
@SecurityRequirement(name = "Bearer Authentication")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    /**
     * POST /data
     *
     * Submit a new sensor reading from a device.
     * The deviceId in the body must belong to the authenticated user.
     *
     * Request body:
     * {
     *   "deviceId": "1",
     *   "temperature": 22.5,
     *   "humidity": 55.0,
     *   "co2": 850.0,
     *   "pm25": 12.0
     * }
     *
     * Response (201 Created):
     * {
     *   "deviceId": "1",
     *   "timestamp": "2024-01-15T10:30:00Z",
     *   "temperature": 22.5,
     *   "humidity": 55.0,
     *   "co2": 850.0,
     *   "pm25": 12.0
     * }
     */
    @PostMapping("/data")
    @Operation(summary = "Submit a sensor reading")
    public ResponseEntity<SensorDataResponse> submitData(
            @Valid @RequestBody SensorDataRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        SensorDataResponse response = sensorDataService.saveSensorData(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /devices/{id}/data?limit=50
     *
     * Get recent sensor readings for a device.
     *
     * @param id    the device ID
     * @param limit how many readings to return (default 50)
     */
    @GetMapping("/devices/{id}/data")
    @Operation(summary = "Get recent sensor readings for a device")
    public ResponseEntity<List<SensorDataResponse>> getDeviceData(
            @PathVariable String id,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SensorDataResponse> data = sensorDataService.getSensorData(id, userDetails.getUsername(), limit);
        return ResponseEntity.ok(data);
    }

    /**
     * GET /devices/{id}/score
     *
     * Get the current air quality score for a device.
     *
     * Response (200 OK):
     * {
     *   "deviceId": "1",
     *   "score": 74.5,
     *   "category": "Moderate",
     *   "explanation": "Score: 74.5/100. CO2 is elevated (950 ppm)."
     * }
     */
    @GetMapping("/devices/{id}/score")
    @Operation(summary = "Get air quality score for a device")
    public ResponseEntity<AirQualityScoreResponse> getAirQualityScore(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        AirQualityScoreResponse score = sensorDataService.getAirQualityScore(id, userDetails.getUsername());
        return ResponseEntity.ok(score);
    }

    /**
     * GET /devices/{id}/trends
     *
     * Get CO2 trend analysis for a device (based on last 20 readings).
     *
     * Response (200 OK):
     * {
     *   "deviceId": "1",
     *   "metric": "CO2",
     *   "trend": "INCREASING",
     *   "averageValue": 920.5,
     *   "latestValue": 1050.0,
     *   "message": "CO2 levels are rising. Consider ventilating the room."
     * }
     */
    @GetMapping("/devices/{id}/trends")
    @Operation(summary = "Get CO2 trend for a device")
    public ResponseEntity<TrendResponse> getTrend(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        TrendResponse trend = sensorDataService.getCo2Trend(id, userDetails.getUsername());
        return ResponseEntity.ok(trend);
    }
}
