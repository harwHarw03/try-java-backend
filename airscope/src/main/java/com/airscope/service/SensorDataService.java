package com.airscope.service;

import com.airscope.dynamodb.SensorData;
import com.airscope.dynamodb.SensorDataRepository;
import com.airscope.dto.SensorDataDto.*;
import com.airscope.model.Device;
import com.airscope.model.User;
import com.airscope.repository.DeviceRepository;
import com.airscope.repository.UserRepository;
import com.airscope.util.AirQualityCalculator;
import com.airscope.util.AppExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SensorDataService - handles storing and analyzing IoT sensor readings.
 *
 * This service bridges two databases:
 *   - PostgreSQL: verifies device/user ownership
 *   - DynamoDB:   stores and retrieves the actual sensor readings
 */
@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository; // DynamoDB
    private final DeviceRepository deviceRepository;          // PostgreSQL
    private final UserRepository userRepository;              // PostgreSQL

    // How many readings to fetch for analytics
    private static final int ANALYTICS_WINDOW = 20;

    /**
     * Save a new sensor reading to DynamoDB.
     *
     * @param request  the sensor reading data
     * @param userEmail the authenticated user's email
     */
    public SensorDataResponse saveSensorData(SensorDataRequest request, String userEmail) {
        // Verify the user owns this device before accepting data from it
        validateDeviceOwnership(request.getDeviceId(), userEmail);

        // Use provided timestamp or generate current time in ISO 8601 format
        String timestamp = (request.getTimestamp() != null && !request.getTimestamp().isBlank())
                ? request.getTimestamp()
                : Instant.now().toString();

        SensorData sensorData = SensorData.builder()
                .deviceId(request.getDeviceId())
                .timestamp(timestamp)
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .co2(request.getCo2())
                .pm25(request.getPm25())
                .build();

        sensorDataRepository.save(sensorData);

        return toResponse(sensorData);
    }

    /**
     * Get recent sensor readings for a device.
     *
     * @param deviceId  the device to query
     * @param userEmail the authenticated user's email
     * @param limit     max number of readings to return
     */
    public List<SensorDataResponse> getSensorData(String deviceId, String userEmail, int limit) {
        validateDeviceOwnership(deviceId, userEmail);

        return sensorDataRepository.findByDeviceId(deviceId, limit)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calculate the air quality score for a device based on its latest reading.
     */
    public AirQualityScoreResponse getAirQualityScore(String deviceId, String userEmail) {
        validateDeviceOwnership(deviceId, userEmail);

        // Get the most recent reading
        List<SensorData> readings = sensorDataRepository.findByDeviceId(deviceId, 1);

        if (readings.isEmpty()) {
            throw new AppExceptions.ResourceNotFoundException(
                    "No sensor data found for device: " + deviceId);
        }

        SensorData latest = readings.get(0);
        double score = AirQualityCalculator.calculateScore(
                latest.getPm25(), latest.getCo2(), latest.getHumidity());

        return AirQualityScoreResponse.builder()
                .deviceId(deviceId)
                .score(Math.round(score * 10.0) / 10.0) // round to 1 decimal
                .category(AirQualityCalculator.getCategory(score))
                .explanation(AirQualityCalculator.getExplanation(
                        latest.getPm25(), latest.getCo2(), latest.getHumidity(), score))
                .build();
    }

    /**
     * Detect the CO2 trend for a device over recent readings.
     */
    public TrendResponse getCo2Trend(String deviceId, String userEmail) {
        validateDeviceOwnership(deviceId, userEmail);

        List<SensorData> readings = sensorDataRepository.findByDeviceId(deviceId, ANALYTICS_WINDOW);

        if (readings.isEmpty()) {
            throw new AppExceptions.ResourceNotFoundException(
                    "No sensor data found for device: " + deviceId);
        }

        String trend = AirQualityCalculator.detectTrend(readings, "co2");
        double average = AirQualityCalculator.movingAverage(readings, "co2", readings.size());
        double latest = readings.get(0).getCo2() != null ? readings.get(0).getCo2() : 0.0;

        String message = switch (trend) {
            case "INCREASING" -> "CO2 levels are rising. Consider ventilating the room.";
            case "DECREASING" -> "CO2 levels are improving.";
            default           -> "CO2 levels are stable.";
        };

        return TrendResponse.builder()
                .deviceId(deviceId)
                .metric("CO2")
                .trend(trend)
                .averageValue(Math.round(average * 10.0) / 10.0)
                .latestValue(latest)
                .message(message)
                .build();
    }

    // --- Private helpers ---

    /**
     * Verify that the deviceId (a String used for DynamoDB) corresponds to a device
     * owned by the authenticated user. Throws exception if not.
     */
    private void validateDeviceOwnership(String deviceId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        // deviceId in DynamoDB is stored as a String, but the DB ID is a Long
        Long deviceIdLong;
        try {
            deviceIdLong = Long.parseLong(deviceId);
        } catch (NumberFormatException e) {
            throw new AppExceptions.BadRequestException("Invalid device ID format: " + deviceId);
        }

        deviceRepository.findByIdAndUserId(deviceIdLong, user.getId())
                .orElseThrow(() -> new AppExceptions.UnauthorizedException(
                        "Device not found or does not belong to you"));
    }

    private SensorDataResponse toResponse(SensorData data) {
        return SensorDataResponse.builder()
                .deviceId(data.getDeviceId())
                .timestamp(data.getTimestamp())
                .temperature(data.getTemperature())
                .humidity(data.getHumidity())
                .co2(data.getCo2())
                .pm25(data.getPm25())
                .build();
    }
}
