package com.airscope.controller;

import com.airscope.dto.DeviceDto.DeviceRequest;
import com.airscope.dto.DeviceDto.DeviceResponse;
import com.airscope.service.DeviceService;
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
 * DeviceController - handles IoT device registration and listing.
 *
 * All endpoints require a valid JWT token (Authorization: Bearer <token>).
 *
 * @AuthenticationPrincipal UserDetails userDetails
 *   Spring automatically injects the currently logged-in user's details.
 *   We use userDetails.getUsername() to get their email.
 *
 * Base URL: /devices
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Manage IoT devices")
@SecurityRequirement(name = "Bearer Authentication")
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * POST /devices
     *
     * Register a new device for the authenticated user.
     *
     * Request body:
     * { "name": "Living Room Sensor" }
     *
     * Response (201 Created):
     * { "id": 1, "name": "Living Room Sensor", "userId": 3 }
     */
    @PostMapping
    @Operation(summary = "Register a new device")
    public ResponseEntity<DeviceResponse> createDevice(
            @Valid @RequestBody DeviceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        DeviceResponse response = deviceService.createDevice(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /devices
     *
     * Get all devices belonging to the authenticated user.
     *
     * Response (200 OK):
     * [
     *   { "id": 1, "name": "Living Room Sensor", "userId": 3 },
     *   { "id": 2, "name": "Office Monitor",     "userId": 3 }
     * ]
     */
    @GetMapping
    @Operation(summary = "Get all my devices")
    public ResponseEntity<List<DeviceResponse>> getMyDevices(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<DeviceResponse> devices = deviceService.getUserDevices(userDetails.getUsername());
        return ResponseEntity.ok(devices);
    }
}
