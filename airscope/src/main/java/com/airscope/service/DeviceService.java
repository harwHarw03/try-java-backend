package com.airscope.service;

import com.airscope.dto.DeviceDto.DeviceRequest;
import com.airscope.dto.DeviceDto.DeviceResponse;
import com.airscope.model.Device;
import com.airscope.model.User;
import com.airscope.repository.DeviceRepository;
import com.airscope.repository.UserRepository;
import com.airscope.util.AppExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DeviceService - handles device creation and retrieval.
 *
 * Important security rule: users can only see/modify THEIR OWN devices.
 * We enforce this by always filtering by both device ID and user ID.
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    /**
     * Create a new device for the logged-in user.
     *
     * @param request device details (name)
     * @param userEmail the email of the authenticated user (from JWT)
     */
    public DeviceResponse createDevice(DeviceRequest request, String userEmail) {
        // Load the user from DB to link the device to them
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        Device device = Device.builder()
                .name(request.getName())
                .user(user)
                .build();

        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    /**
     * Get all devices belonging to the logged-in user.
     *
     * @param userEmail the email of the authenticated user (from JWT)
     */
    public List<DeviceResponse> getUserDevices(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        return deviceRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing device's name.
     *
     * @param deviceId the device ID
     * @param request updated device details
     * @param userEmail the email of the authenticated user
     */
    public DeviceResponse updateDevice(Long deviceId, DeviceRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        Device device = deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));

        device.setName(request.getName());
        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    /**
     * Delete a device and all its associated data.
     *
     * @param deviceId the device ID
     * @param userEmail the email of the authenticated user
     */
    public void deleteDevice(Long deviceId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        Device device = deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));

        deviceRepository.delete(device);
    }

    /**
     * Convert a Device entity to a DeviceResponse DTO.
     * We do this to avoid exposing internal entity structure to clients.
     */
    private DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .userId(device.getUser().getId())
                .build();
    }
}
