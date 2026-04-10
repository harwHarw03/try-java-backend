package com.airscope.util;

import com.airscope.model.Device;
import com.airscope.model.User;
import com.airscope.repository.DeviceRepository;
import com.airscope.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceOwnershipValidator {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    /**
     * Validates that the device belongs to the user.
     * Throws appropriate exception if validation fails.
     *
     * @param deviceId the device ID to validate
     * @param userEmail the user's email
     * @return the validated Device entity
     */
    public Device validateOwnership(String deviceId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        Long deviceIdLong;
        try {
            deviceIdLong = Long.parseLong(deviceId);
        } catch (NumberFormatException e) {
            throw new AppExceptions.BadRequestException("Invalid device ID format: " + deviceId);
        }

        return deviceRepository.findByIdAndUserId(deviceIdLong, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));
    }

    /**
     * Validates ownership for a device with Long ID.
     */
    public Device validateOwnership(Long deviceId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        return deviceRepository.findByIdAndUserId(deviceId, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));
    }
}
