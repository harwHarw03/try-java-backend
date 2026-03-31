package com.airscope.service;

import com.airscope.dto.AlertDto.AlertRequest;
import com.airscope.dto.AlertDto.AlertResponse;
import com.airscope.model.Alert;
import com.airscope.model.Device;
import com.airscope.model.User;
import com.airscope.repository.AlertRepository;
import com.airscope.repository.DeviceRepository;
import com.airscope.repository.UserRepository;
import com.airscope.util.AppExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertService - handles alert creation and retrieval.
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    /**
     * Create a new alert on a device the user owns.
     * We verify the device belongs to the requesting user before creating the alert.
     */
    public AlertResponse createAlert(AlertRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        // Security check: ensure the device belongs to this user
        Device device = deviceRepository.findByIdAndUserId(request.getDeviceId(), user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));

        Alert alert = Alert.builder()
                .type(request.getType().toUpperCase()) // normalize: "co2" → "CO2"
                .threshold(request.getThreshold())
                .device(device)
                .build();

        Alert saved = alertRepository.save(alert);
        return toResponse(saved);
    }

    /**
     * Get all alerts for a specific device.
     * Again, we check the device belongs to the user first.
     */
    public List<AlertResponse> getDeviceAlerts(Long deviceId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        // Verify ownership
        deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));

        return alertRepository.findByDeviceId(deviceId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .type(alert.getType())
                .threshold(alert.getThreshold())
                .deviceId(alert.getDevice().getId())
                .build();
    }
}
